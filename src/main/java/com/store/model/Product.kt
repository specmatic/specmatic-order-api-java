package com.store.model

import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Positive
import java.util.concurrent.atomic.AtomicInteger

data class Product(
    @field:NotNull val name: String = "",
    @field:NotNull val type: String = "gadget",
    @field:NotNull @field:Positive @field:Min(1) @field:Max(101) val inventory: Int = 0,
    val id: Int = idGenerator.getAndIncrement()
) {
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