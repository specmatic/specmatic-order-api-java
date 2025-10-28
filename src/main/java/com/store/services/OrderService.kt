package com.store.services

import com.store.exceptions.ValidationException
import com.store.model.DB
import com.store.model.Id
import com.store.model.Order
import com.store.model.OrderStatus
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class OrderService {
    @Autowired
    lateinit var hashService: HashService

    fun createOrder(order: Order, idempotencyKey: UUID): Id {
        val idempotencyHash = hashService.hashData(order, idempotencyKey)
        return DB.addOrder(order, idempotencyHash)
    }

    fun getOrder(id: Int): Order {
        return DB.getOrder(id)
    }

    fun deleteOrder(id: Int) {
        DB.deleteOrder(id)
    }

    fun updateOrder(id: Int, order: Order) {
        if (id == 0) throw ValidationException("Product id cannot be null")
        DB.updateOrder(id, order)
    }

    fun findOrders(status: OrderStatus?, productid: Int?): List<Order> {
        return DB.findOrders(status, productid)
    }

    fun createBulkOrders(orders: List<Order>, idempotencyKey: UUID): List<Id> {
        val idempotencyHash = hashService.hashData(orders, idempotencyKey)
        return DB.createBulkOrders(orders, idempotencyHash)
    }
}