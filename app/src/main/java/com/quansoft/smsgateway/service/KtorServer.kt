package com.quansoft.smsgateway.service

import android.os.Build
import com.quansoft.smsgateway.data.SmsDao
import com.quansoft.smsgateway.data.SmsMessage
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.call
import io.ktor.server.application.install
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.routing
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.flow.first
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.util.UUID

@Serializable
data class SendRequest(val to: String?, val message: String?, val messageID: String? )

fun Application.configureRouting(smsDao: SmsDao) {


    install(ContentNegotiation) {
        json(Json {
            prettyPrint = true
            isLenient = true
            ignoreUnknownKeys = true
        })
    }

    routing {
        val deviceToken by lazy {

            "${Build.BOARD}-${Build.ID}-${Build.BOOTLOADER}"
        }


        fun isAuthorized(authHeader: String?): Boolean {
            return authHeader == deviceToken
        }

        get("/messages") {
            if (!isAuthorized(call.request.headers["Authorization"])) {
                call.respond(HttpStatusCode.Forbidden, "Unauthorized")
                return@get
            }
            val messages = smsDao.getAllMessages().first()
            call.respond(messages)
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
            if (uid == null){
                uid = UUID.randomUUID().toString()
            }

            if (to.isNullOrBlank() || messageContent.isNullOrBlank()) {
                call.respond(HttpStatusCode.BadRequest, "Missing 'to' or 'message' parameter.")
                return@post
            }

            val message = SmsMessage(
                id = uid,
                recipient = to,
                content = messageContent,
                status = "queued",
                timestamp = System.currentTimeMillis()
            )

            smsDao.insert(message)

            call.respond(mapOf("messageId" to message.id, "status" to "queued"))
        }
    }
}
