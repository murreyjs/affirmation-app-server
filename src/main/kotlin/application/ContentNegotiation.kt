package application

import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*
import kotlinx.serialization.json.Json

/**
 * Configures content negotiation for the Ktor application.
 */
fun Application.configureContentNegotiation() {
    install(ContentNegotiation) {
        json(Json { prettyPrint = true })
    }
}