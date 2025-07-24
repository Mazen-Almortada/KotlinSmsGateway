package com.quansoft.smsgateway.domain.usecase

import com.quansoft.smsgateway.domain.repository.MessageRepository
import javax.inject.Inject

class UpdateMessageStatusUseCase @Inject constructor(
    private val messageRepository: MessageRepository
) {
    suspend operator fun invoke(messageId: String, newStatus: String) {
        messageRepository.updateStatus(messageId, newStatus)
    }
}