package com.gymia.domain.model

data class WarmUpScheme(val percentage: Float, val reps: Int)

object WarmUpProtocol {
    private const val STANDARD_THRESHOLD_KG = 60f

    private val STANDARD = listOf(
        WarmUpScheme(percentage = 0.40f, reps = 10),
        WarmUpScheme(percentage = 0.60f, reps = 5),
        WarmUpScheme(percentage = 0.75f, reps = 3),
        WarmUpScheme(percentage = 0.90f, reps = 1)
    )

    private val LIGHT = listOf(
        WarmUpScheme(percentage = 0.50f, reps = 10),
        WarmUpScheme(percentage = 0.70f, reps = 5)
    )

    fun schemeFor(targetLoadKg: Float): List<WarmUpScheme> =
        if (targetLoadKg >= STANDARD_THRESHOLD_KG) STANDARD else LIGHT
}
