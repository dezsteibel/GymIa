package com.gymia.data.repository

import android.content.Context
import com.gymia.di.WidgetEntryPoint
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.runBlocking

class WidgetRepository(private val context: Context) {

    private val workoutRepository by lazy {
        EntryPointAccessors.fromApplication(
            context.applicationContext,
            WidgetEntryPoint::class.java
        ).workoutRepository()
    }

    fun getNextWorkoutDay(): Pair<String, List<String>>? = runBlocking {
        val plans = workoutRepository.getAllPlansOnce()
        val latestPlan = plans.lastOrNull() ?: return@runBlocking null
        val planWithDays = workoutRepository.getPlanWithDaysOnce(latestPlan.id)
            ?: return@runBlocking null
        val firstDay = planWithDays.days.minByOrNull { it.order }
            ?: return@runBlocking null
        val exercises = workoutRepository.getDayExercisesOnce(firstDay.id)
            .take(3)
            .map { it.exercise.name }
        Pair(firstDay.label, exercises)
    }
}
