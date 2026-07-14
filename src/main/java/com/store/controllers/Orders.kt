package com.store.controllers

import com.store.model.*
import com.store.services.OrderService
import jakarta.validation.Valid
import jakarta.validation.constraints.NotNull
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*
import java.util.UUID

@Validated
@RestController
class Orders {
    @Autowired
    lateinit var orderService: OrderService

    @PostMapping("/orders", consumes = [MediaType.APPLICATION_JSON_VALUE])
    fun create(
        @Valid @RequestBody request: NewOrderRequest,
        @NotNull @RequestHeader("Idempotency-Key", required = true) idempotencyKey: UUID,
        @AuthenticationPrincipal user: User,
    ): ResponseEntity<Id> {
        val orderId = orderService.createOrder(request, idempotencyKey)
        return ResponseEntity(orderId, HttpStatus.CREATED)
    }

    @GetMapping("/orders/{id}")
    fun get(@PathVariable("id") id: Int): Order {
        return orderService.getOrder(id)
    }

    @DeleteMapping("/orders/{id}")
    fun delete(@PathVariable("id") id: Int, @AuthenticationPrincipal user: User): ResponseEntity<String> {
        orderService.deleteOrder(id)
        return ResponseEntity(HttpStatus.OK)
    }

    @PatchMapping("/orders/{id}", consumes = [MediaType.APPLICATION_JSON_VALUE])
    fun update(
        @PathVariable("id") id: Int,
        @Valid @RequestBody request: UpdateOrderRequest,
        @AuthenticationPrincipal user: User,
    ): ResponseEntity<String> {
        orderService.updateOrder(id, request)
        return ResponseEntity(HttpStatus.OK)
    }

    @GetMapping("/orders")
    fun search(
        @RequestParam(name = "status", required = false) status: OrderStatus?,
        @RequestParam(name = "productid", required = false) productid: Int?,
    ): List<Order> = orderService.findOrders(status, productid)
}
