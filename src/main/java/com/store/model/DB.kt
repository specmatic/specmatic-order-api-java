package com.store.model

import com.example.inventory.GetInventoryRequest
import com.example.inventory.InventoryService
import com.store.exceptions.IdempotencyConflictException
import jakarta.xml.ws.BindingProvider
import java.util.UUID

private data class IdempotentRecord<T>(val bodyHash: String, val result: T)

object DB {
    private val IDEMPOTENCY_STORE: MutableMap<UUID, IdempotentRecord<Any>> = mutableMapOf()
    private val USERS: Map<String, User> = mapOf("API-TOKEN-SPEC" to User("Hari"))
    private var ORDERS: MutableMap<Int, Order> = mutableMapOf(10 to Order(10, 2, OrderStatus.pending, 10), 20 to Order(10, 1, OrderStatus.pending, 20))
    private var PRODUCT_IMAGE: MutableMap<Int, String> = mutableMapOf(10 to "https://example.com/image.jpg", 20 to "https://example.com/image.jpg")
    private var PRODUCTS: MutableMap<Int, Product> = mutableMapOf(
        10 to Product("XYZ Phone", ProductType.gadget, 10, 10),
        20 to Product("Gemini", ProductType.other, 10, 20),
        30 to Product("Margarita Pizza", ProductType.food, 10, 30),
        40 to Product("Learn Specmatic", ProductType.book, 10, 40),
    )

    fun userCount(): Int {
        return USERS.values.count()
    }

    fun <T> executeIdempotent(idempotencyKey: UUID, bodyHash: String, block: () -> T): T {
        val existing = IDEMPOTENCY_STORE[idempotencyKey]

        if (existing != null) {
            if (existing.bodyHash != bodyHash) {
                throw IdempotencyConflictException("Idempotency key $idempotencyKey is already used with different request body")
            }

            @Suppress("UNCHECKED_CAST")
            return existing.result as T
        }

        val result = block()
        IDEMPOTENCY_STORE[idempotencyKey] = IdempotentRecord(bodyHash, result as Any)
        return result
    }

    fun resetDB() {
        PRODUCTS = mutableMapOf(
            10 to Product("XYZ Phone", ProductType.gadget, 10, 10),
            20 to Product("Gemini", ProductType.other, 10, 20),
            30 to Product("Cleaner", ProductType.gadget, 10, 30),
        )
        ORDERS = mutableMapOf(10 to Order(10, 2, OrderStatus.pending, 10), 20 to Order(10, 1, OrderStatus.pending, 20))
        IDEMPOTENCY_STORE.clear()
    }

    fun addProduct(product: Product, idempotencyKey: UUID, bodyHash: String): Id {
        return executeIdempotent(idempotencyKey, bodyHash) {
            val productWithUniqueId = product.ensureUniqueId(PRODUCTS.keys)
            PRODUCTS[productWithUniqueId.id] = productWithUniqueId
            return@executeIdempotent Id(productWithUniqueId.id)
        }
    }

    fun findProduct(id: Int): Product {
        if (id !in PRODUCTS) throw NoSuchElementException("Product Id $id does not exist")
        return PRODUCTS.getValue(id)
    }

    fun updateProduct(id: Int, update: Product) {
        if (id !in PRODUCTS) throw NoSuchElementException("Product Id $id does not exist")
        PRODUCTS[id] = run {
            PRODUCTS.getValue(id)
            Product(update.name, update.type, update.inventory)
        }
    }

    fun deleteProduct(id: Int) {
        if (id !in PRODUCTS) throw NoSuchElementException("Product Id $id does not exist")
        PRODUCTS.remove(id)
    }

    fun findProducts(name: String?, type: ProductType?, status: String?): List<Product> {
        val products = PRODUCTS.filter { (id, product) ->
            product.name == name || product.type == type || inventoryStatus(id) == status
        }.values.toList()

        val inventoryService = InventoryService()
        val inventoryServicePort = inventoryService.inventoryServicePort.apply {
            (this as BindingProvider).requestContext[BindingProvider.ENDPOINT_ADDRESS_PROPERTY] = "http://localhost:9000/ws"
        }

        return products.map { product ->
            val getInventoryRequest = GetInventoryRequest().also {
                it.productid = product.id
            }

            val response = inventoryServicePort.getInventory(getInventoryRequest)

            product.copy(inventory = response.inventory)
        }
    }

    private fun inventoryStatus(productid: Int): String {
        return when (PRODUCTS.getValue(productid).inventory) {
            0 -> "sold"
            else -> "available"
        }
    }

    fun addOrder(order: Order, idempotencyKey: UUID, bodyHash: String): Id {
        return executeIdempotent(idempotencyKey, bodyHash) {
            reserveProductInventory(order.productid, order.count)
            val orderWithUniqueId = order.ensureUniqueId(ORDERS.keys)
            ORDERS[orderWithUniqueId.id] = orderWithUniqueId
            return@executeIdempotent Id(orderWithUniqueId.id)
        }
    }

    fun getOrder(id: Int): Order {
        if (id !in ORDERS) throw NoSuchElementException("Order with id $id not found")
        return ORDERS.getValue(id)
    }

    fun deleteOrder(id: Int) {
        if (id !in ORDERS) throw NoSuchElementException("Order with id $id not found")
        ORDERS.remove(id)
    }

    fun findOrders(status: OrderStatus?, productId: Int?): List<Order> {
        return ORDERS.filter { (_, order) ->
            order.status == status || order.productid == productId
        }.values.toList()
    }

    fun updateOrder(id: Int, updatedOrder: Order) {
        if (id !in ORDERS) throw NoSuchElementException("Order with id $id not found")
        ORDERS[id] = updatedOrder
    }

    fun reserveProductInventory(productId: Int, count: Int) {
        if (productId !in PRODUCTS) throw NoSuchElementException("Product with id $productId does not exist")
        val updatedProduct = PRODUCTS.getValue(productId).let {
            it.copy(inventory = it.inventory - count)
        }

        PRODUCTS[productId] = updatedProduct
    }

    fun updateProductImage(id: Int, imageFileName: String) {
        if (id !in PRODUCTS) throw NoSuchElementException("Product with id $id does not exist")
        PRODUCT_IMAGE[id] = imageFileName
    }
}
