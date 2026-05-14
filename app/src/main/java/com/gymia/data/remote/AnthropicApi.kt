package com.gymia.data.remote

import com.gymia.data.remote.dto.AiRequest
import com.gymia.data.remote.dto.AiResponse
import retrofit2.http.Body
import retrofit2.http.POST

interface AnthropicApi {
    @POST("v1/messages")
    suspend fun generateMessage(@Body request: AiRequest): AiResponse
}
