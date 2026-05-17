package com.gymia.domain.model

data class SessionDetail(
    val sessionId: Long,
    val planName: String?,
    val dayLabel: String?,
    val date: Long,
    val durationMinutes: Int,
    val exercises: List<ExerciseWithSets>
) {
    val totalVolume: Float
        get() = exercises
            .flatMap { it.sets }
            .filter { it.completed }
            .sumOf { (it.reps * it.loadKg).toDouble() }
            .toFloat()
}

data class ExerciseWithSets(
    val exerciseName: String,
    val sets: List<SetDetail>
)

data class SetDetail(
    val setNumber: Int,
    val reps: Int,
    val loadKg: Float,
    val completed: Boolean
)
