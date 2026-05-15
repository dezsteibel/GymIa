package com.gymia.data.repository

import com.gymia.data.model.WorkoutSession
import com.gymia.data.remote.AnthropicApi
import com.gymia.data.remote.dto.AiMessage
import com.gymia.data.remote.dto.AiRequest
import com.gymia.data.remote.dto.WorkoutCycleDto
import com.gymia.data.remote.dto.toDomain
import com.gymia.domain.model.WorkoutCycle
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject

class AiRepository @Inject constructor(
    private val anthropicApi: AnthropicApi,
    private val json: Json
) {
    suspend fun generateCycle(sessions: List<WorkoutSession>): Result<WorkoutCycle> = runCatching {
        val request = AiRequest(
            system = SYSTEM_PROMPT,
            messages = listOf(AiMessage(role = "user", content = buildUserMessage(sessions)))
        )
        val responseText = anthropicApi.generateMessage(request).content.first().text
        json.decodeFromString<WorkoutCycleDto>(responseText).toDomain()
    }

    private fun buildUserMessage(sessions: List<WorkoutSession>): String = """
        Here is my recent workout history:
        ${json.encodeToString(sessions)}

        Please generate my next periodized training cycle based on this data.
        Consider my performance trends, stagnation points, and progression opportunities.
        Respond only with the JSON object as specified.
    """.trimIndent()

    private companion object {
        const val SYSTEM_PROMPT =
            "You are an expert strength and conditioning coach specializing in periodized training " +
                "for advanced lifters. The user trains for strength and hypertrophy (mixed). " +
                "They are advanced (3+ years). They plan their own workouts without a personal trainer.\n\n" +
                "When asked to generate a training cycle, analyze the provided workout history and " +
                "respond ONLY with a valid JSON object. Do not include markdown, code blocks, or any " +
                "explanation outside the JSON.\n\n" +
                "The JSON must follow this exact structure:\n" +
                "{\n" +
                "  \"cycle_name\": \"string\",\n" +
                "  \"duration_weeks\": number,\n" +
                "  \"general_notes\": \"string\",\n" +
                "  \"days\": [\n" +
                "    {\n" +
                "      \"day_label\": \"string\",\n" +
                "      \"exercises\": [\n" +
                "        {\n" +
                "          \"name\": \"string\",\n" +
                "          \"sets\": number,\n" +
                "          \"reps_target\": \"string\",\n" +
                "          \"load_suggestion_kg\": number,\n" +
                "          \"progression_note\": \"string\"\n" +
                "        }\n" +
                "      ]\n" +
                "    }\n" +
                "  ]\n" +
                "}"
    }
}
