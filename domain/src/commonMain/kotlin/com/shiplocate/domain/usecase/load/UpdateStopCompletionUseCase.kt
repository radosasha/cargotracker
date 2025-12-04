package com.shiplocate.domain.usecase.load

import com.shiplocate.domain.model.load.Stop
import com.shiplocate.domain.repository.AuthRepository
import com.shiplocate.domain.repository.LoadRepository

/**
 * Use case to update stop completion status
 * Updates the completion field for a stop
 */
class UpdateStopCompletionUseCase(
    private val loadRepository: LoadRepository,
    private val authRepository: AuthRepository,
) {
    /**
     * Update stop completion
     * Automatically retrieves auth token from preferences
     * @param stopId Server's stop ID
     * @param completion Completion status (0 = NOT_COMPLETED, 1 = COMPLETED)
     * @return Result with updated Stop
     */
    suspend operator fun invoke(
        stopId: Long,
        completion: Int,
    ): Result<Stop> {
        println("✅ UpdateStopCompletionUseCase: Updating stop completion for stop $stopId to $completion")

        // Get auth token
        val authSession = authRepository.getSession()
        val token = authSession?.token

        if (token == null) {
            println("❌ UpdateStopCompletionUseCase: Not authenticated")
            return Result.failure(Exception("Not authenticated"))
        }

        return loadRepository.updateStopCompletion(token, stopId, completion)
    }
}

