package com.store.model

data class ProductResponse(
    val name: String,
    val type: ProductType,
    val inventory: Int,
    val id: Int
) {
    constructor(product: Product, inventory: Int) : this(
        name = product.name,
        type = product.type,
        inventory = inventory,
        id = product.id
    )
}
