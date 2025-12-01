package com.store.model.inventory

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import org.jetbrains.annotations.NotNull

@JacksonXmlRootElement(localName = "Inventory")
data class Inventory(
    @field:NotNull val productId: Int = 0,
    @field:NotNull @field:Min(1) @field:Max(101) val inventory: Int = 0
)
