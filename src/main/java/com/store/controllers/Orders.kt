package com.store.controllers

import com.store.exceptions.NotFoundException
import com.store.model.*
import com.store.services.OrderService
import jakarta.validation.Valid
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import java.util.NoSuchElementException
import java.util.UUID

@RestController
class Orders {
    @Autowired
    lateinit var orderService: OrderService

    @PostMapping("/orders")
    fun create(
        @Valid @RequestBody order: Order,
        @RequestHeader("Idempotency-Key") idempotencyKey: UUID,
        @AuthenticationPrincipal user: User,
    ): ResponseEntity<Id> {
        println(order.status.toString())
        val orderId = orderService.createOrder(order, idempotencyKey)
        return ResponseEntity(orderId, HttpStatus.CREATED)
    }

    @GetMapping("/orders/{id}")
    fun get(@PathVariable("id") id: Int): Order {
        try {
            return orderService.getOrder(id)
        } catch (e: NoSuchElementException) {
            throw NotFoundException(e.message!!)
        }
    }

    @DeleteMapping("/orders/{id}")
    fun delete(@PathVariable("id") id: Int, @AuthenticationPrincipal user: User): ResponseEntity<String> {
        orderService.deleteOrder(id)
        return ResponseEntity(HttpStatus.OK)
    }

    @PatchMapping("/orders/{id}")
    fun update(
        @PathVariable("id") id: Int,
        @Valid @RequestBody order: Order,
        @AuthenticationPrincipal user: User
    ): ResponseEntity<String> {
        println(order.status.toString())
        orderService.updateOrder(id, order)
        return ResponseEntity(HttpStatus.OK)
    }

    @GetMapping("/orders")
    fun search(
        @RequestParam(name = "status", required = false) status: OrderStatus?,
        @RequestParam(name = "productid", required = false) productid: Int?
    ): List<Order> = orderService.findOrders(status, productid)
}
