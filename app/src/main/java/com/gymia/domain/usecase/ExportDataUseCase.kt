package com.gymia.domain.usecase

import com.gymia.data.repository.WorkoutRepository
import com.gymia.domain.model.CardioExport
import com.gymia.domain.model.ExerciseExport
import com.gymia.domain.model.ExportData
import com.gymia.domain.model.PlanExport
import com.gymia.domain.model.SessionExport
import com.gymia.domain.model.SetExport
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.time.Instant
import java.time.format.DateTimeFormatter
import javax.inject.Inject

class ExportDataUseCase @Inject constructor(
    private val repository: WorkoutRepository
) {
    private val json = Json { prettyPrint = true; encodeDefaults = true }

    suspend operator fun invoke(): String {
        val exportData = ExportData(
            exportedAt = DateTimeFormatter.ISO_INSTANT.format(Instant.now()),
            exercises = repository.getAllExercisesOnce().map {
                ExerciseExport(it.id, it.name, it.muscleGroup, it.equipmentType)
            },
            plans = repository.getAllPlansOnce().map {
                PlanExport(it.id, it.name, it.createdAt, it.source)
            },
            sessions = repository.getAllSessionsOnce().map {
                SessionExport(it.id, it.planId, it.dayId, it.date, it.durationMinutes, it.notes)
            },
            sets = repository.getAllSetsOnce().map {
                SetExport(it.id, it.sessionId, it.exerciseId, it.setNumber, it.reps, it.loadKg, it.completed)
            },
            cardioRecords = repository.getAllCardioOnce().map {
                CardioExport(it.id, it.date, it.activityType, it.durationMinutes, it.distanceKm, it.notes)
            }
        )
        return json.encodeToString(exportData)
    }
}
