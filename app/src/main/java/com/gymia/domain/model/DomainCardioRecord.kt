package com.gymia.domain.model

data class DomainCardioRecord(
    val id: Long,
    val date: Long,
    val activityType: String,
    val durationMinutes: Int,
    val distanceKm: Float?,
    val notes: String?
)
