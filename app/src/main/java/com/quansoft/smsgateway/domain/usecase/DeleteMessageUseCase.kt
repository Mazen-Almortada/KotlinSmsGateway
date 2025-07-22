package com.quansoft.smsgateway.domain.usecase

import com.quansoft.smsgateway.domain.model.Message
import com.quansoft.smsgateway.domain.repository.MessageRepository

/**
 * A simple use case for deleting a single message.
 */
class DeleteMessageUseCase(
    private val messageRepository: MessageRepository
) {
    suspend operator fun invoke(message: Message) {
        messageRepository.deleteMessage(message)
    }
}
