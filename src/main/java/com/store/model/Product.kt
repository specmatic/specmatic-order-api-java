package com.store.model

import java.time.LocalDateTime
import java.util.concurrent.atomic.AtomicInteger

data class Product(
    val name: String,
    val type: ProductType,
    val inventory: Int,
    val id: Int = 0,
    val createdOn: LocalDateTime = LocalDateTime.now()
) {
    constructor(request: NewProductRequest) : this(0, request)

    constructor(id: Int, request: NewProductRequest) : this(request.name!!, request.type!!, request.inventory!!, id, LocalDateTime.now())

    fun ensureUniqueId(ids: Collection<Int>) = copy(
        id = generateSequence(idGenerator::incrementAndGet).first { it !in ids },
    )

    companion object {
        val idGenerator: AtomicInteger = AtomicInteger(0)
    }
}

enum class ProductType {
    book,
    food,
    gadget,
    other
}