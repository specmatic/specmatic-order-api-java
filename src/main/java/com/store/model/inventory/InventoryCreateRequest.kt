package com.store.model.inventory

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement

@JacksonXmlRootElement(localName = "InventoryCreateRequest")
data class InventoryCreateRequest(val productId: Int, val inventory: Int)
