package com.siga.backend.api.chat

import kotlinx.serialization.Serializable

@Serializable
data class ChatRequest(
    val message: String
)

@Serializable
data class ChatResponse(
    val success: Boolean,
    val response: String? = null,
    val message: String? = null
)

