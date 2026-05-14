package com.gymia.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class AiResponse(
    val content: List<AiContent>
)

@Serializable
data class AiContent(
    val type: String,
    val text: String
)
