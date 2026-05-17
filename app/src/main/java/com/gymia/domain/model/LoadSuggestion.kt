package com.gymia.domain.model

data class LoadSuggestion(
    val suggestedLoad: Float,
    val trend: Trend,
    val confidence: Float
)

enum class Trend { PROGRESSING, STABLE, REGRESSING }
