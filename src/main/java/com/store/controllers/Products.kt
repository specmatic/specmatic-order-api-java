package com.store.controllers

import com.store.model.Id
import com.store.model.NewProductRequest
import com.store.model.Product
import com.store.model.ProductType
import com.store.model.User
import com.store.services.ProductService
import jakarta.validation.Valid
import jakarta.validation.constraints.NotNull
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import java.util.UUID

@Validated
@RestController
open class Products {
    @Autowired
    lateinit var productService: ProductService

    @PatchMapping("/products/{id}")
    @Validated
    fun update(
        @PathVariable("id") id: Int,
        @Valid @RequestBody request: NewProductRequest,
        @AuthenticationPrincipal user: User,
    ): ResponseEntity<String> {
        productService.updateProduct(id, request)
        return ResponseEntity(HttpStatus.OK)
    }

    @GetMapping("/products/{id}")
    fun get(@PathVariable("id") id: Int): Product {
        return productService.getProduct(id)
    }

    @PostMapping("/products")
    fun create(
        @Valid @RequestBody request: NewProductRequest,
        @NotNull @RequestHeader("Idempotency-Key", required = true) idempotencyKey: UUID,
        @AuthenticationPrincipal user: User,
    ): ResponseEntity<Id> {
        val productId = productService.addProduct(request, idempotencyKey)
        return ResponseEntity(productId, HttpStatus.CREATED)
    }

    @DeleteMapping("/products/{id}")
    fun delete(@PathVariable("id") id: Int, @AuthenticationPrincipal user: User): ResponseEntity<String> {
        productService.deleteProduct(id)
        return ResponseEntity(HttpStatus.OK)
    }

    @GetMapping("/products")
    fun search(
        @RequestParam(name = "name", required = false) name: String?,
        @RequestParam(name = "type", required = false) type: ProductType?,
        @RequestParam(name = "status", required = false) status: String?,
    ): ResponseEntity<List<Product>> {
        // An exception thrown by some internal bug...
        if (name == "unknown")
            return ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR)
        val products = productService.findProducts(name, type, status)
        return ResponseEntity(products, HttpStatus.OK)
    }

    @PutMapping("/products/{id}/image", consumes = ["multipart/form-data"])
    fun uploadImage(@PathVariable("id") id: Int, @RequestPart("image") image: MultipartFile): ResponseEntity<Map<String, Any>> {
        productService.addImage(id, image.originalFilename, image.bytes)
        val response = mapOf("message" to "Product image updated successfully")
        return ResponseEntity(response, HttpStatus.OK)
    }
}
