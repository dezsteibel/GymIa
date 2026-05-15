package com.gymia.data.remote.dto

import com.gymia.domain.model.CycleDay
import com.gymia.domain.model.CycleExercise
import com.gymia.domain.model.WorkoutCycle
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class WorkoutCycleDto(
    @SerialName("cycle_name") val cycleName: String,
    @SerialName("duration_weeks") val durationWeeks: Int,
    @SerialName("general_notes") val generalNotes: String,
    val days: List<CycleDayDto>
)

@Serializable
data class CycleDayDto(
    @SerialName("day_label") val dayLabel: String,
    val exercises: List<CycleExerciseDto>
)

@Serializable
data class CycleExerciseDto(
    val name: String,
    val sets: Int,
    @SerialName("reps_target") val repsTarget: String,
    @SerialName("load_suggestion_kg") val loadSuggestionKg: Double,
    @SerialName("progression_note") val progressionNote: String
)

fun WorkoutCycleDto.toDomain(): WorkoutCycle = WorkoutCycle(
    cycleName = cycleName,
    durationWeeks = durationWeeks,
    generalNotes = generalNotes,
    days = days.map { it.toDomain() }
)

fun CycleDayDto.toDomain(): CycleDay = CycleDay(
    dayLabel = dayLabel,
    exercises = exercises.map { it.toDomain() }
)

fun CycleExerciseDto.toDomain(): CycleExercise = CycleExercise(
    name = name,
    sets = sets,
    repsTarget = repsTarget,
    loadSuggestionKg = loadSuggestionKg,
    progressionNote = progressionNote
)
