package com.gymia.domain.usecase

import com.gymia.data.repository.WorkoutTemplateRepository
import com.gymia.domain.model.WorkoutTemplate
import javax.inject.Inject

class GetWorkoutTemplatesUseCase @Inject constructor(
    private val repository: WorkoutTemplateRepository
) {
    operator fun invoke(): List<WorkoutTemplate> = repository.getTemplates()
}
