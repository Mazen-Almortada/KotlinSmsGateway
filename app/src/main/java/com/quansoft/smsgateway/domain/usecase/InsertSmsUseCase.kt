package com.quansoft.smsgateway.domain.usecase

import com.quansoft.smsgateway.domain.model.Message
import com.quansoft.smsgateway.domain.repository.MessageRepository

/**
 * A use case dedicated to the business logic of sending a single message.
 * It ensures the message is created correctly and passed to the repository.
 */
class InsertSmsUseCase(
    private val messageRepository: MessageRepository
) {
    // We will make the UseCase callable as a function
    suspend operator fun invoke(message: Message): Message {

        // Note: We need to add an 'insert' method to the MessageRepository interface
        messageRepository.insert(message)
        return message
    }
}