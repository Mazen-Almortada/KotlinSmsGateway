package com.quansoft.smsgateway.service

import com.quansoft.smsgateway.domain.repository.SettingsRepository
import com.quansoft.smsgateway.domain.usecase.GetMessagesWithDetailsUseCase
import com.quansoft.smsgateway.domain.usecase.InsertAllSmsUseCase
import com.quansoft.smsgateway.domain.usecase.InsertCampaignUseCase
import com.quansoft.smsgateway.domain.usecase.InsertSmsUseCase

import io.ktor.server.engine.ApplicationEngine
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

/**
 * A dedicated class responsible for managing the Ktor server lifecycle.
 * It depends only on domain layer UseCases and is completely decoupled
 * from the data layer's implementation details (DAOs).
 */
@Singleton
class SmsGatewayServer @Inject constructor(
    private val insertSmsUseCase: InsertSmsUseCase,
    private val insertCampaignUseCase: InsertCampaignUseCase,
    private val insertAllSmsUseCase: InsertAllSmsUseCase,
    private val getMessagesWithDetailsUseCase: GetMessagesWithDetailsUseCase,
    private val settingsRepository: SettingsRepository
) {

    private var server: ApplicationEngine? = null

    suspend fun start() {
        if (server == null) {
            val port = settingsRepository.getServerPort().first()
            val authToken = settingsRepository.getAuthToken().first()

            server = embeddedServer(Netty, port = port, host = "0.0.0.0") {
                configureRouting(
                    insertSmsUseCase = insertSmsUseCase,
                    insertAllSmsUseCase = insertAllSmsUseCase,
                    getMessagesWithDetailsUseCase = getMessagesWithDetailsUseCase,
                    insertCampaignUseCase = insertCampaignUseCase,
                    authToken = authToken
                )
            }.start(wait = false)
        }
    }

    fun stop() {
        server?.stop(1000, 2000)
        server = null
    }
}