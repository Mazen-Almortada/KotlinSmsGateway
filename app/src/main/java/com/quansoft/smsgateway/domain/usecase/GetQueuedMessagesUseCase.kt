package com.quansoft.smsgateway.domain.usecase

import com.quansoft.smsgateway.domain.model.Message
import com.quansoft.smsgateway.domain.repository.MessageRepository
import javax.inject.Inject

class GetQueuedMessagesUseCase @Inject constructor(
    private val messageRepository: MessageRepository
) {
    suspend operator fun invoke(): List<Message> {
        return messageRepository.getQueuedMessages()
    }
}