package com.store.services

import com.store.backend.InventoryService
import com.store.filestorage.LocalFileSystem
import com.store.model.DB
import com.store.model.Id
import com.store.model.NewProductRequest
import com.store.model.Product
import com.store.model.ProductType
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class ProductService {
    @Autowired
    lateinit var hashService: HashService

    @Autowired
    lateinit var inventoryService: InventoryService

    fun getProduct(id: Int): Product {
        val product = DB.findProduct(id)
        val inventory = inventoryService.retrieveInventory(id)
        return product.copy(inventory = inventory.inventory)
    }

    fun updateProduct(id: Int, request: NewProductRequest) {
        val product = Product(id, request)
        DB.updateProduct(id, product)
        inventoryService.updateInventory(id, request.inventory ?: 0)
    }

    fun addProduct(request: NewProductRequest, idempotencyKey: UUID): Id {
        val bodyHash = hashService.hashData(request)
        val product = DB.ensureUniqueId(Product(request))
        inventoryService.createInventory(product.id, request.inventory ?: 0)
        val id = DB.addProduct(product, idempotencyKey, bodyHash)
        return id
    }

    fun deleteProduct(id: Int) {
        DB.deleteProduct(id)
    }

    fun findProducts(name:String?, type: ProductType?, status:String?): List<Product> {
        val products = DB.findProducts(name, type)
        return products.map { product ->
            val inv = inventoryService.retrieveInventory(product.id)
            product.copy(inventory = inv.inventory)
        }.filter { product ->
            if (status == null) return@filter true
            val currentStatus = if (product.inventory > 0) "available" else "sold"
            currentStatus == status
        }
    }

    fun addImage(id: Int, imageFileName: String, bytes: ByteArray) {
        val canonicalImageFilePath = LocalFileSystem.saveImage(imageFileName, bytes)
        DB.updateProductImage(id, canonicalImageFilePath)
    }
}