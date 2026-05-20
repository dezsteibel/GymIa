package com.gymia.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class UserProfile(
    val name: String = "",
    val goal: Goal = Goal.MIXED,
    val experienceLevel: ExperienceLevel = ExperienceLevel.INTERMEDIATE,
    val trainingDaysPerWeek: Int = 4,
    val bodyWeightKg: Float? = null,
    val notes: String? = null
)
