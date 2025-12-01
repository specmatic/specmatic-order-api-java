package com.store.services

import com.store.backend.InventoryService
import com.store.model.DB
import com.store.model.Id
import com.store.model.NewOrderRequest
import com.store.model.Order
import com.store.model.OrderStatus
import com.store.model.UpdateOrderRequest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class OrderService {
    @Autowired
    lateinit var hashService: HashService

    @Autowired
    lateinit var inventoryService: InventoryService

    fun createOrder(request: NewOrderRequest, idempotencyKey: UUID): Id {
        val bodyHash = hashService.hashData(request)
        val order = DB.ensureUniqueId(Order(request))
        inventoryService.reserveInventory(order.productid, order.count)
        return DB.addOrder(order, idempotencyKey, bodyHash)
    }

    fun getOrder(id: Int): Order {
        return DB.getOrder(id)
    }

    fun deleteOrder(id: Int) {
        DB.deleteOrder(id)
    }

    fun updateOrder(id: Int, request: UpdateOrderRequest) {
        val order = Order(id, request)
        DB.updateOrder(id, order)
    }

    fun findOrders(status: OrderStatus?, productid: Int?): List<Order> {
        return DB.findOrders(status, productid)
    }
}
