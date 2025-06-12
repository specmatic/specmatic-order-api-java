package com.store.model

import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Positive
import java.util.concurrent.atomic.AtomicInteger

class Order(@field:Positive val productid: Int = 0, @field:Positive val count: Int = 0, @field:NotNull var status: OrderStatus = OrderStatus.pending, val id: Int = idGenerator.getAndIncrement()) {
    companion object {
        val idGenerator: AtomicInteger = AtomicInteger(1)
    }
}

enum class OrderStatus {
    pending,
    fulfilled,
    cancelled
}