package com.quansoft.smsgateway.domain.usecase

import com.quansoft.smsgateway.domain.model.Message
import com.quansoft.smsgateway.domain.repository.MessageRepository

class InsertAllSmsUseCase(private val messageRepository: MessageRepository) {
    /**
     * Inserts a list of messages into the repository.
     * This use case is designed to handle bulk insertions efficiently.
     *
     * @param messages The list of messages to be inserted.
     */
    suspend operator fun invoke(messages: List<Message>) {
        messageRepository.insertAll(messages)
    }
}