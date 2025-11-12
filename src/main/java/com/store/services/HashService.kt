package com.store.services

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.stereotype.Service
import java.security.MessageDigest

@Service
class HashService {
    private val objectMapper: ObjectMapper = ObjectMapper()

    fun hashData(data: Any): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val serialized = objectMapper.writeValueAsString(data)
        val hashBytes = digest.digest(serialized.toByteArray(Charsets.UTF_8))
        return hashBytes.joinToString("") { "%02x".format(it) }
    }
}
