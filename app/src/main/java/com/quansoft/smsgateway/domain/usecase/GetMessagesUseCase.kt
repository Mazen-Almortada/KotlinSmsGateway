package com.quansoft.smsgateway.domain.usecase

import com.quansoft.smsgateway.domain.model.Message
import com.quansoft.smsgateway.domain.repository.MessageRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetMessagesUseCase @Inject constructor(
    private val messageRepository: MessageRepository
) {
    operator fun invoke(): Flow<List<Message>> {
        return messageRepository.getAllMessages()
    }
}