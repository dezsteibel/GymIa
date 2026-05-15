package com.gymia.domain.model

data class ProgressSummary(
    val exerciseProgresses: List<ExerciseProgress>,
    val weeklyVolumes: List<WeeklyVolumePoint>,
    val cycleSessionCount: Int,
    val cycleTotalVolume: Float
)
