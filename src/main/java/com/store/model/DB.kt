package com.store.model

import com.store.exceptions.UnrecognizedTypeException
import jakarta.validation.ValidationException

object DB {
    private var PRODUCTS: MutableMap<Int, Product> =
        mutableMapOf(
            10 to Product("XYZ Phone", "gadget", 10, 10),
            20 to Product("Gemini", "dog", 10, 20),
            30 to Product("Cleaner", "gadget", 10, 30)
        )
    private var PRODUCT_IMAGE: MutableMap<Int, String> =
        mutableMapOf(10 to "https://example.com/image.jpg", 20 to "https://example.com/image.jpg")
    private var ORDERS: MutableMap<Int, Order> =
        mutableMapOf(10 to Order(10, 2, OrderStatus.pending, 10), 20 to Order(10, 1, OrderStatus.pending, 20))
    private val USERS: Map<String, User> = mapOf("API-TOKEN-SPEC" to User("Hari"))
    private val IDEMPOTENCY_STORE: MutableMap<String, Any> = mutableMapOf()

    fun userCount(): Int {
        return USERS.values.count()
    }

    fun <T> executeIdempotent(idempotencyHash: String, block: () -> T): T {
        @Suppress("UNCHECKED_CAST")
        return IDEMPOTENCY_STORE[idempotencyHash] as? T ?: run {
            val result = block()
            IDEMPOTENCY_STORE[idempotencyHash] = result as Any
            result
        }
    }

    fun resetDB() {
        PRODUCTS = mutableMapOf(
            10 to Product("XYZ Phone", "gadget", 10, 10),
            20 to Product("Gemini", "dog", 10, 20),
            30 to Product("Cleaner", "gadget", 10, 30),
        )
        ORDERS = mutableMapOf(10 to Order(10, 2, OrderStatus.pending, 10), 20 to Order(10, 1, OrderStatus.pending, 20))
        IDEMPOTENCY_STORE.clear()
    }

    fun addProduct(product: Product, idempotencyHash: String): Id {
        return executeIdempotent(idempotencyHash) {
            PRODUCTS[product.id] = product
            return@executeIdempotent Id(product.id)
        }
    }

    fun findProduct(id: Int): Product = PRODUCTS.getValue(id)

    fun updateProduct(id: Int, update: Product) {
        if (id !in PRODUCTS) throw ValidationException("Product Id $id does not exist")
        PRODUCTS[id] = run {
            PRODUCTS.getValue(id)
            Product(update.name, update.type, update.inventory)
        }
    }

    fun deleteProduct(id: Int) {
        PRODUCTS.remove(id)
    }

    fun findProducts(name: String?, type: String?, status: String?): List<Product> {
        if (type != null && type !in listOf("book", "food", "gadget", "other")) throw UnrecognizedTypeException(type)
        return PRODUCTS.filter { (id, product) ->
            product.name == name || product.type == type || inventoryStatus(id) == status
        }.values.toList()
    }

    private fun inventoryStatus(productid: Int): String {
        return when (PRODUCTS.getValue(productid).inventory) {
            0 -> "sold"
            else -> "available"
        }
    }

    fun addOrder(order: Order, idempotencyHash: String): Id {
        return executeIdempotent(idempotencyHash) {
            reserveProductInventory(order.productid, order.count)
            ORDERS[order.id] = order
            return@executeIdempotent Id(order.id)
        }
    }

    fun getOrder(id: Int): Order = ORDERS.getValue(id)

    fun deleteOrder(id: Int) {
        ORDERS.remove(id)
    }

    fun findOrders(status: OrderStatus?, productId: Int?): List<Order> {
        return ORDERS.filter { (_, order) ->
            order.status == status || order.productid == productId
        }.values.toList()
    }

    fun updateOrder(id: Int, updatedOrder: Order) {
        if (id !in ORDERS) throw ValidationException("Order Id $id does not exist")
        ORDERS[id] = updatedOrder
    }

    fun reserveProductInventory(productId: Int, count: Int) {
        if (productId !in PRODUCTS) throw ValidationException("Product Id $productId does not exist")
        val updatedProduct = PRODUCTS.getValue(productId).let {
            it.copy(inventory = it.inventory - count)
        }

        PRODUCTS[productId] = updatedProduct
    }

    fun updateProductImage(id: Int, imageFileName: String) {
        if (id !in PRODUCT_IMAGE) throw ValidationException("Product Id $id does not exist")
        PRODUCT_IMAGE[id] = imageFileName
    }

    fun createBulkOrders(orders: List<Order>, idempotencyHash: String): List<Id> {
        return executeIdempotent(idempotencyHash) {
            orders.map { order ->
                reserveProductInventory(order.productid, order.count)
                ORDERS[order.id] = order
                Id(order.id)
            }
        }
    }
}
