package com.store.model.inventory

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement

@JacksonXmlRootElement(localName = "InventoryUpdateRequest")
data class InventoryUpdateRequest(val inventory: Int)
