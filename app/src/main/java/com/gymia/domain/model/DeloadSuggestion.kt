package com.gymia.domain.model

data class DeloadSuggestion(
    val shouldDeload: Boolean,
    val reason: DeloadReason,
    val affectedExercises: List<String>,
    val suggestedDeloadWeeks: Int = 1
)

enum class DeloadReason {
    STAGNATION,
    REGRESSION,
    HIGH_VOLUME,
    NONE
}
