package com.gymia.domain.model

data class SessionSummary(
    val sessionId: Long,
    val date: Long,
    val durationMinutes: Int,
    val totalSets: Int,
    val planId: Long?,
    val dayId: Long?,
    val planName: String?,
    val notes: String?
)
