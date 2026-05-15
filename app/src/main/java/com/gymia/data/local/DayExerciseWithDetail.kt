package com.gymia.data.local

import androidx.room.Embedded
import androidx.room.Relation
import com.gymia.data.model.DayExercise
import com.gymia.data.model.Exercise

data class DayExerciseWithDetail(
    @Embedded val dayExercise: DayExercise,
    @Relation(parentColumn = "exerciseId", entityColumn = "id")
    val exercise: Exercise
)
