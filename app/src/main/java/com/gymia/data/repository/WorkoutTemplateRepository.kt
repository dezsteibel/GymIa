package com.gymia.data.repository

import com.gymia.domain.model.Goal
import com.gymia.domain.model.TemplateDayInput
import com.gymia.domain.model.TemplateExerciseInput
import com.gymia.domain.model.WorkoutTemplate
import javax.inject.Inject

class WorkoutTemplateRepository @Inject constructor() {

    fun getTemplates(): List<WorkoutTemplate> = listOf(
        strongLifts5x5(), pushPullLegs(), upperLowerSplit(), fullBody3x()
    )

    private fun strongLifts5x5() = WorkoutTemplate(
        id = "stronglifts_5x5",
        name = "StrongLifts 5×5",
        description = "Classic strength program with linear progression on compound lifts.",
        targetGoal = Goal.STRENGTH,
        daysPerWeek = 3,
        days = listOf(sl5DayA(), sl5DayB())
    )

    private fun sl5DayA() = TemplateDayInput("Day A", listOf(
        ex("Squat", "legs", "barbell", 5),
        ex("Bench Press", "chest", "barbell", 5),
        ex("Barbell Row", "back", "barbell", 5)
    ))

    private fun sl5DayB() = TemplateDayInput("Day B", listOf(
        ex("Squat", "legs", "barbell", 5),
        ex("Overhead Press", "shoulders", "barbell", 5),
        ex("Deadlift", "back", "barbell", 1)
    ))

    private fun pushPullLegs() = WorkoutTemplate(
        id = "push_pull_legs",
        name = "Push / Pull / Legs",
        description = "High-volume hypertrophy split targeting each muscle group twice per week.",
        targetGoal = Goal.HYPERTROPHY,
        daysPerWeek = 6,
        days = listOf(pplPush(), pplPull(), pplLegs())
    )

    private fun pplPush() = TemplateDayInput("Push", listOf(
        ex("Bench Press", "chest", "barbell", 4),
        ex("Overhead Press", "shoulders", "barbell", 3),
        ex("Lateral Raise", "shoulders", "dumbbell", 3),
        ex("Tricep Pushdown", "arms", "cable", 3)
    ))

    private fun pplPull() = TemplateDayInput("Pull", listOf(
        ex("Deadlift", "back", "barbell", 3),
        ex("Pull-up", "back", "bodyweight", 4),
        ex("Cable Row", "back", "cable", 3),
        ex("Bicep Curl", "arms", "dumbbell", 3)
    ))

    private fun pplLegs() = TemplateDayInput("Legs", listOf(
        ex("Squat", "legs", "barbell", 4),
        ex("Romanian Deadlift", "legs", "barbell", 3),
        ex("Leg Press", "legs", "machine", 3),
        ex("Calf Raise", "legs", "machine", 4)
    ))

    private fun upperLowerSplit() = WorkoutTemplate(
        id = "upper_lower",
        name = "Upper / Lower Split",
        description = "Balanced strength and hypertrophy across 4 days per week.",
        targetGoal = Goal.MIXED,
        daysPerWeek = 4,
        days = listOf(ulUpperA(), ulUpperB(), ulLowerA(), ulLowerB())
    )

    private fun ulUpperA() = TemplateDayInput("Upper A", listOf(
        ex("Bench Press", "chest", "barbell", 4),
        ex("Barbell Row", "back", "barbell", 4),
        ex("Overhead Press", "shoulders", "barbell", 3),
        ex("Pull-up", "back", "bodyweight", 3)
    ))

    private fun ulUpperB() = TemplateDayInput("Upper B", listOf(
        ex("Incline Press", "chest", "dumbbell", 3),
        ex("Cable Row", "back", "cable", 3),
        ex("Lateral Raise", "shoulders", "dumbbell", 3),
        ex("Bicep Curl", "arms", "dumbbell", 3)
    ))

    private fun ulLowerA() = TemplateDayInput("Lower A", listOf(
        ex("Squat", "legs", "barbell", 4),
        ex("Romanian Deadlift", "legs", "barbell", 3),
        ex("Leg Press", "legs", "machine", 3)
    ))

    private fun ulLowerB() = TemplateDayInput("Lower B", listOf(
        ex("Front Squat", "legs", "barbell", 3),
        ex("Leg Curl", "legs", "machine", 3),
        ex("Walking Lunge", "legs", "dumbbell", 3),
        ex("Calf Raise", "legs", "machine", 4)
    ))

    private fun fullBody3x() = WorkoutTemplate(
        id = "full_body_3x",
        name = "Full Body 3×",
        description = "Full-body sessions three times per week with compound movements.",
        targetGoal = Goal.MIXED,
        daysPerWeek = 3,
        days = listOf(fb3DayA(), fb3DayB(), fb3DayC())
    )

    private fun fb3DayA() = TemplateDayInput("Day A", listOf(
        ex("Squat", "legs", "barbell", 3),
        ex("Bench Press", "chest", "barbell", 3),
        ex("Deadlift", "back", "barbell", 3),
        ex("Pull-up", "back", "bodyweight", 3)
    ))

    private fun fb3DayB() = TemplateDayInput("Day B", listOf(
        ex("Squat", "legs", "barbell", 3),
        ex("Overhead Press", "shoulders", "barbell", 3),
        ex("Barbell Row", "back", "barbell", 3),
        ex("Dip", "chest", "bodyweight", 3)
    ))

    private fun fb3DayC() = TemplateDayInput("Day C", listOf(
        ex("Squat", "legs", "barbell", 3),
        ex("Bench Press", "chest", "barbell", 3),
        ex("Romanian Deadlift", "legs", "barbell", 3),
        ex("Chin-up", "back", "bodyweight", 3)
    ))

    private fun ex(name: String, muscle: String, equipment: String, sets: Int) =
        TemplateExerciseInput(name, muscle, equipment, sets)
}
