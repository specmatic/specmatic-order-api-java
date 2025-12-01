package com.store.backend

import com.store.model.inventory.Inventory
import com.store.model.inventory.InventoryCreateRequest
import com.store.model.inventory.InventoryUpdateRequest
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.client.SimpleClientHttpRequestFactory
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate

private enum class API(val method: HttpMethod, val url: String) {
    CREATE_INVENTORY(HttpMethod.POST, "/inventory"),
    RETRIEVE_INVENTORY(HttpMethod.GET, "/inventory"),
    UPDATE_INVENTORY(HttpMethod.PUT, "/inventory");

    fun finalizeUrl(baseUrl: String, id: Int? = null): String {
        return when (this) {
            CREATE_INVENTORY -> listOf(baseUrl, this.url).joinToString(separator = "/")
            else -> listOfNotNull(baseUrl, this.url, id).joinToString(separator = "/")
        }
    }
}

@Service
class InventoryService {
    @Value("\${inventory.api.url}")
    lateinit var inventoryApiBaseUrl: String

    private val restTemplateWithDefaultTimeout: RestTemplate by lazy {
        val restTemplate = RestTemplate()
        val requestFactory = SimpleClientHttpRequestFactory()
        requestFactory.setConnectTimeout(1000)
        requestFactory.setReadTimeout(1000)
        restTemplate.requestFactory = requestFactory
        restTemplate
    }

    fun createInventory(productId: Int, inventory: Int): Inventory {
        val request = InventoryCreateRequest(productId, inventory)
        val apiUrl = API.CREATE_INVENTORY.finalizeUrl(inventoryApiBaseUrl)
        val requestEntity = HttpEntity(request)
        val response = restTemplateWithDefaultTimeout.exchange(
            apiUrl,
            API.CREATE_INVENTORY.method,
            requestEntity,
            Inventory::class.java,
        )

        return response.body ?: throw IllegalStateException("Failed to create inventory")
    }

    fun retrieveInventory(productId: Int): Inventory {
        val apiUrl = API.RETRIEVE_INVENTORY.finalizeUrl(inventoryApiBaseUrl, productId)
        val requestEntity = HttpEntity<Any>(HttpHeaders())
        val response = restTemplateWithDefaultTimeout.exchange(
            apiUrl,
            API.RETRIEVE_INVENTORY.method,
            requestEntity,
            Inventory::class.java,
        )

        return response.body ?: throw IllegalStateException("Failed to retrieve inventory")
    }

    fun updateInventory(productId: Int, inventory: Int): Inventory {
        val request = InventoryUpdateRequest(inventory)
        val apiUrl = API.UPDATE_INVENTORY.finalizeUrl(inventoryApiBaseUrl, productId)
        val requestEntity = HttpEntity(request)
        val response = restTemplateWithDefaultTimeout.exchange(
            apiUrl,
            API.UPDATE_INVENTORY.method,
            requestEntity,
            Inventory::class.java,
        )

        return response.body ?: throw IllegalStateException("Failed to update inventory")
    }

    fun reserveInventory(productId: Int, count: Int) {
        val currentInventory = retrieveInventory(productId)
        val newCount = currentInventory.inventory - count
        updateInventory(productId, newCount)
    }
}
