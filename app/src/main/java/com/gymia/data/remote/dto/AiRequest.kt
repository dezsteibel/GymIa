package com.gymia.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AiRequest(
    val model: String = "claude-sonnet-4-20250514",
    @SerialName("max_tokens") val maxTokens: Int = 4096,
    val system: String,
    val messages: List<AiMessage>
)

@Serializable
data class AiMessage(
    val role: String,
    val content: String
)
