package com.gymia.domain.model

data class GlobalStats(
    val totalSessions: Int,
    val totalVolumeKg: Float,
    val totalSetsLogged: Int,
    val heaviestSetKg: Float,
    val heaviestSetExercise: String,
    val mostFrequentExercise: String,
    val currentStreak: Int,
    val longestStreak: Int
)
