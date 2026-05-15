package com.gymia.domain.model

data class ExerciseProgress(
    val exerciseId: Long,
    val exerciseName: String,
    val history: List<ProgressPoint>,
    val personalRecord: Float,
    val personalRecordDate: Long
)
