package com.gymia.domain.usecase

import com.gymia.data.repository.UserProfileRepository
import com.gymia.domain.model.UserProfile
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetUserProfileUseCase @Inject constructor(
    private val repository: UserProfileRepository
) {
    operator fun invoke(): Flow<UserProfile> = repository.getProfile()
}
