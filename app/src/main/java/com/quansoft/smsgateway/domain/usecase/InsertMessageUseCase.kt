package com.quansoft.smsgateway.domain.usecase

import com.quansoft.smsgateway.domain.model.Message
import com.quansoft.smsgateway.domain.repository.MessageRepository
import java.util.UUID
import javax.inject.Inject

class InsertMessageUseCase @Inject constructor(
    private val messagesRepository: MessageRepository,
) {
    suspend operator fun invoke(message: Message): String {

        messagesRepository.insert(message)
        return message.id
    }
}