package com.store.model

import java.util.concurrent.atomic.AtomicInteger

data class Product(
    val name: String,
    val type: ProductType,
    val id: Int = 0
) {
    constructor(request: NewProductRequest) : this(0, request)

    constructor(id: Int, request: NewProductRequest) : this(request.name!!, request.type!!, id)

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