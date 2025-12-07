package com.store.services

import com.store.controllers.DateRange
import com.store.filestorage.LocalFileSystem
import com.store.model.DB
import com.store.model.Id
import com.store.model.NewProductRequest
import com.store.model.Product
import com.store.model.ProductResponse
import com.store.model.ProductType
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class ProductService {
    @Autowired
    lateinit var hashService: HashService

    @Autowired
    lateinit var inventoryServiceClient: InventoryServiceClient

    fun getProduct(id: Int): ProductResponse {
        return DB.findProduct(id, inventoryServiceClient)
    }

    fun updateProduct(id: Int, request: NewProductRequest) {
        val product = Product(id, request)
        DB.updateProduct(id, product)
    }

    fun addProduct(request: NewProductRequest, idempotencyKey: UUID): Id {
        val bodyHash = hashService.hashData(request)
        val inventory = request.inventory!!
        val product = Product(request)
        val productId = DB.addProduct(product, idempotencyKey, bodyHash)

        inventoryServiceClient.addInventory(productId.id, inventory)

        return productId
    }

    fun deleteProduct(id: Int) {
        DB.deleteProduct(id)
    }

    fun findProducts(name: String?, type: ProductType?, status: String?, dateRange: DateRange?): List<ProductResponse> {
        return DB.findProducts(name, type, status, inventoryServiceClient).filter {
            dateRange?.contains(it.createdOn) ?: true
        }
    }

    fun addImage(id: Int, imageFileName: String, bytes: ByteArray) {
        val canonicalImageFilePath = LocalFileSystem.saveImage(imageFileName, bytes)
        DB.updateProductImage(id, canonicalImageFilePath)
    }
}
