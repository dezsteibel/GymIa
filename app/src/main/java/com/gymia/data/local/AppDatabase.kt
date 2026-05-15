package com.gymia.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.gymia.data.model.CardioRecord
import com.gymia.data.model.Exercise
import com.gymia.data.model.SetRecord
import com.gymia.data.model.WorkoutDay
import com.gymia.data.model.WorkoutPlan
import com.gymia.data.model.WorkoutSession

@Database(
    entities = [
        Exercise::class,
        WorkoutPlan::class,
        WorkoutDay::class,
        WorkoutSession::class,
        SetRecord::class,
        CardioRecord::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun exerciseDao(): ExerciseDao
    abstract fun workoutDao(): WorkoutDao
    abstract fun sessionDao(): SessionDao
    abstract fun cardioDao(): CardioDao
}
