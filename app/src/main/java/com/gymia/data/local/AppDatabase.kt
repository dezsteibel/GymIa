package com.gymia.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.gymia.data.model.CardioRecord
import com.gymia.data.model.DayExercise
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
        DayExercise::class,
        WorkoutSession::class,
        SetRecord::class,
        CardioRecord::class
    ],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun exerciseDao(): ExerciseDao
    abstract fun workoutDao(): WorkoutDao
    abstract fun sessionDao(): SessionDao
    abstract fun cardioDao(): CardioDao

    companion object {
        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE set_records ADD COLUMN notes TEXT")
            }
        }
    }
}
