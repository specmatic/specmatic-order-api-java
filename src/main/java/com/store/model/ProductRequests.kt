package com.store.model

import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Positive

data class NewProductRequest(
    @field:NotNull val name: String? = null,
    @field:NotNull val type: ProductType? = null,
    @field:NotNull @field:Positive @field:Min(1) @field:Max(101) val inventory: Int? = null
)
