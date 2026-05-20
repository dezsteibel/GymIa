package com.gymia.domain.usecase

import com.gymia.data.repository.UserProfileRepository
import com.gymia.domain.model.UserProfile
import javax.inject.Inject

class SaveUserProfileUseCase @Inject constructor(
    private val repository: UserProfileRepository
) {
    suspend operator fun invoke(profile: UserProfile) = repository.saveProfile(profile)
}
