package com.store.services

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

    fun getProduct(id: Int): Product {
        return DB.findProduct(id)
    }

    fun updateProduct(id: Int, request: NewProductRequest) {
        val product = Product(id, request)
        DB.updateProduct(id, product)
    }

    fun addProduct(request: NewProductRequest, idempotencyKey: UUID): Id {
        val bodyHash = hashService.hashData(request)
        val product = Product(request)
        return DB.addProduct(product, idempotencyKey, bodyHash)
    }

    fun deleteProduct(id: Int) {
        DB.deleteProduct(id)
    }

    fun findProducts(name:String?, type: ProductType?, status:String?): List<Product> {
        return DB.findProducts(name, type, status)
    }

    fun addImage(id: Int, imageFileName: String, bytes: ByteArray) {
        val canonicalImageFilePath = LocalFileSystem.saveImage(imageFileName, bytes)
        DB.updateProductImage(id, canonicalImageFilePath)
    }
}