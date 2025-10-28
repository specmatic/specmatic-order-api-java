package com.store.services

import com.store.exceptions.ValidationException
import com.store.filestorage.LocalFileSystem
import com.store.model.DB
import com.store.model.Id
import com.store.model.Product
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

    fun updateProduct(id: Int, product:Product){
        if (id == 0) throw ValidationException("Product id cannot be null")
        DB.updateProduct(id, product)
    }

    fun addProduct(product: Product, idempotencyKey: UUID): Id {
        val idempotencyHash = hashService.hashData(product, idempotencyKey)
        return DB.addProduct(product, idempotencyHash)
    }

    fun deleteProduct(id: Int) {
        DB.deleteProduct(id)
    }

    fun findProducts(name:String?, type:String?, status:String?): List<Product> {
        return DB.findProducts(name, type, status)
    }

    fun addImage(id: Int, imageFileName: String, bytes: ByteArray) {
        val canonicalImageFilePath = LocalFileSystem.saveImage(imageFileName, bytes)
        DB.updateProductImage(id, canonicalImageFilePath)
    }
}