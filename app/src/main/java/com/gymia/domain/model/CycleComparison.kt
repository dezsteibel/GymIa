package com.gymia.domain.model

data class CycleComparison(
    val currentCycle: CycleStats,
    val previousCycle: CycleStats?,
    val volumeDeltaPercent: Float,
    val loadDeltaPercent: Float,
    val mostImprovedExercise: String?,
    val mostRegressedExercise: String?
)

data class CycleStats(
    val planName: String,
    val startDate: Long,
    val endDate: Long,
    val totalSessions: Int,
    val totalVolumeKg: Float,
    val averageLoadKg: Float,
    val exerciseStats: List<ExerciseStats>
)

data class ExerciseStats(
    val exerciseName: String,
    val maxLoadKg: Float,
    val totalVolume: Float,
    val sessionsLogged: Int
)
