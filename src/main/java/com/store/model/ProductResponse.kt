package com.store.model

import java.time.LocalDate

data class ProductResponse(
    val name: String,
    val type: ProductType,
    val inventory: Int,
    val id: Int,
    val createdOn: LocalDate
) {
    constructor(product: Product, inventory: Int) : this(
        name = product.name,
        type = product.type,
        inventory = inventory,
        id = product.id,
        createdOn = product.createdOn
    )
}
