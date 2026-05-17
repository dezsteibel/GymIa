package com.gymia.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class ExportData(
    val exportedAt: String,
    val exercises: List<ExerciseExport>,
    val plans: List<PlanExport>,
    val sessions: List<SessionExport>,
    val sets: List<SetExport>,
    val cardioRecords: List<CardioExport>
)

@Serializable
data class ExerciseExport(val id: Long, val name: String, val muscleGroup: String, val equipmentType: String)

@Serializable
data class PlanExport(val id: Long, val name: String, val createdAt: Long, val source: String)

@Serializable
data class SessionExport(val id: Long, val planId: Long?, val dayId: Long?, val date: Long, val durationMinutes: Int, val notes: String?)

@Serializable
data class SetExport(val id: Long, val sessionId: Long, val exerciseId: Long, val setNumber: Int, val reps: Int, val loadKg: Float, val completed: Boolean)

@Serializable
data class CardioExport(val id: Long, val date: Long, val activityType: String, val durationMinutes: Int, val distanceKm: Float?, val notes: String?)
