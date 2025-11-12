package com.store.model

import java.util.concurrent.atomic.AtomicInteger

data class Order(
    val productid: Int,
    val count: Int,
    val status: OrderStatus = OrderStatus.pending,
    val id: Int = 0
) {
    constructor(request: NewOrderRequest) : this(request.productid!!, request.count!!)

    constructor(id: Int, request: UpdateOrderRequest) : this(request.productid!!, request.count!!, request.status!!, id)

    fun ensureUniqueId(ids: Collection<Int>) = copy(
        id = generateSequence(idGenerator::incrementAndGet).first { it !in ids },
    )

    companion object {
        val idGenerator: AtomicInteger = AtomicInteger(1)
    }
}

enum class OrderStatus {
    pending,
    fulfilled,
    cancelled
}
