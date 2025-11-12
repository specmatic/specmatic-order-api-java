package com.store.model

import jakarta.validation.constraints.NotNull

data class NewOrderRequest(
    @field:NotNull val productid: Int? = null,
    @field:NotNull val count: Int? = null
)

data class UpdateOrderRequest(
    @field:NotNull val productid: Int? = null,
    @field:NotNull val count: Int? = null,
    @field:NotNull val status: OrderStatus? = null,
)
