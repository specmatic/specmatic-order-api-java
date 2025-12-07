package com.store.services

import com.example.inventory.*
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import jakarta.xml.ws.BindingProvider

@Service
class InventoryServiceClient {
    @Value("\${inventory.service.url}")
    private lateinit var inventoryServiceUrl: String

    private fun getInventoryServicePort(): InventoryServicePortType {
        val wsdlURL = javaClass.getResource("/wsdls/inventory.wsdl")
            ?: error("Inventory WSDL not found in resources")
        val service = InventoryService(wsdlURL)
        val port = service.inventoryServicePort
        val bindingProvider = port as BindingProvider

        bindingProvider.requestContext[BindingProvider.ENDPOINT_ADDRESS_PROPERTY] = inventoryServiceUrl
        return port
    }

    fun addInventory(productId: Int, inventory: Int) {
        val request = AddInventoryRequest()
        request.productid = productId
        request.inventory = inventory

        val port = getInventoryServicePort()
        port.addInventory(request)
    }

    fun reduceInventory(productId: Int, inventory: Int) {
        val request = ReduceInventoryRequest()
        request.productid = productId
        request.inventory = inventory

        val port = getInventoryServicePort()
        port.reduceInventory(request)
    }

    fun getInventory(productId: Int): Int {
        val request = GetInventoryRequest()
        request.productid = productId

        val port = getInventoryServicePort()
        val response = port.getInventory(request)
        return response.inventory
    }
}
