package com.gymia.domain.usecase

import com.gymia.domain.model.WarmUpProtocol
import com.gymia.domain.model.WarmUpSet
import kotlin.math.roundToInt
import javax.inject.Inject

class GenerateWarmUpUseCase @Inject constructor() {

    operator fun invoke(targetLoadKg: Float): List<WarmUpSet> =
        WarmUpProtocol.schemeFor(targetLoadKg)
            .mapIndexed { index, scheme ->
                WarmUpSet(
                    setNumber = index + 1,
                    percentage = scheme.percentage,
                    suggestedLoadKg = roundToNearest2Point5(targetLoadKg * scheme.percentage),
                    reps = scheme.reps
                )
            }

    private fun roundToNearest2Point5(value: Float): Float =
        ((value / 2.5f).roundToInt() * 2.5f)
}
