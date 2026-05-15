package com.gymia.domain.model

data class DomainSetWithDate(
    val exerciseId: Long,
    val reps: Int,
    val loadKg: Float,
    val date: Long,
    val sessionId: Long
)
