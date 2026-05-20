package com.gymia.data.repository

import android.content.Context
import androidx.room.Room
import com.gymia.data.local.AppDatabase
import kotlinx.coroutines.runBlocking

class WidgetRepository(private val context: Context) {

    private val database: AppDatabase by lazy {
        Room.databaseBuilder(context.applicationContext, AppDatabase::class.java, "gymia_database")
            .addMigrations(AppDatabase.MIGRATION_2_3)
            .fallbackToDestructiveMigration()
            .build()
    }

    fun getNextWorkoutDay(): Pair<String, List<String>>? = runBlocking {
        val plans = database.workoutDao().getAllPlansOnce()
        val latestPlan = plans.lastOrNull() ?: return@runBlocking null
        val planWithDays = database.workoutDao().getPlanWithDaysById(latestPlan.id)
            ?: return@runBlocking null
        val firstDay = planWithDays.days.minByOrNull { it.order } ?: return@runBlocking null
        val exercises = database.workoutDao().getExercisesWithDetailForDayOnce(firstDay.id)
            .take(3)
            .map { it.exercise.name }
        Pair(firstDay.label, exercises)
    }
}
