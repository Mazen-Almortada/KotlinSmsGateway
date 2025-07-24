package com.quansoft.smsgateway.service

import com.quansoft.smsgateway.domain.model.Message
import com.quansoft.smsgateway.domain.usecase.GetQueuedMessagesUseCase
import com.quansoft.smsgateway.domain.usecase.InsertCampaignUseCase
import com.quansoft.smsgateway.domain.usecase.InsertMessageUseCase
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.call
import io.ktor.server.application.install
import io.ktor.server.plugins.callloging.CallLogging
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.routing
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.slf4j.event.Level
import java.util.UUID

@Serializable
data class SendRequest(val to: String?, val message: String?, val messageID: String?)

fun Application.configureRouting(
    getQueuedMessagesUseCase: GetQueuedMessagesUseCase,
    insertCampaignUseCase: InsertCampaignUseCase,
    insertMessageUseCase: InsertMessageUseCase,

    authToken: String
) {

    install(CallLogging) {
        level = Level.INFO
    }

    install(ContentNegotiation) {
        json(Json {
            prettyPrint = true
            isLenient = true
            ignoreUnknownKeys = true
        })
    }

    routing {
        // The check now uses the token passed from the service
        fun isAuthorized(authHeader: String?): Boolean {
            return authHeader == authToken
        }

        get("/messages") {
            if (!isAuthorized(call.request.headers["Authorization"])) {
                call.respond(HttpStatusCode.Forbidden, "Unauthorized")
                return@get
            }
            val messages = getQueuedMessagesUseCase().first()
            call.respond(messages)
        }
        post("/send-bulk") {
            if (!isAuthorized(call.request.headers["Authorization"])) {
                call.respond(HttpStatusCode.Forbidden, "Unauthorized")
                return@post
            }

            val request = call.receive<BulkRequest>()

            if (request.messages.isEmpty()) {
                call.respond(HttpStatusCode.BadRequest, "The 'Bulk Messages' list cannot be empty.")
                return@post
            }


            insertCampaignUseCase(request)

            // 2. Map the incoming messages to our SmsMessage entity, linking them by bulkId.
            val newMessages = request.messages.map { bulkMsg ->
                Message(
                    id = UUID.randomUUID().toString(),
                    recipient = bulkMsg.to,
                    content = bulkMsg.message,
                    status = "queued",
                    timestamp = System.currentTimeMillis(),
                    bulkId = request.bulkId // Link to the campaign
                )
            }


            call.respond(
                HttpStatusCode.Accepted,
                mapOf(
                    "status" to "success",
                    "message" to "${newMessages.size} messages have been queued for sending.",
                    "bulkId" to request.bulkId
                )
            )
        }

        post("/send") {
            if (!isAuthorized(call.request.headers["Authorization"])) {
                call.respond(HttpStatusCode.Forbidden, "Unauthorized")
                return@post
            }

            val request = call.receive<SendRequest>()
            val to = request.to
            val messageContent = request.message
            var uid = request.messageID
            if (uid == null) {
                uid = UUID.randomUUID().toString()
            }

            if (to.isNullOrBlank() || messageContent.isNullOrBlank()) {
                call.respond(HttpStatusCode.BadRequest, "Missing 'to' or 'message' parameter.")
                return@post
            }

            val message = Message(
                id = uid,
                recipient = to,
                content = messageContent,
                status = "queued",
                timestamp = System.currentTimeMillis(),
                bulkId = null // No campaign link for single messages
            )

            insertMessageUseCase(message)

            call.respond(mapOf("messageId" to message.id, "status" to "queued"))
        }
    }
}
