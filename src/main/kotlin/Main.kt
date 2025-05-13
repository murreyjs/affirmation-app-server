
import application.configureContentNegotiation
import application.configureCors
import application.configureRouting
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import model.*

fun main() {
    val httpPort = System.getenv(Constants.Env.PORT)?.toInt() ?: 8080

    val environment = applicationEngineEnvironment {

        // Configure HTTP connector (optional)
        connector {
            port = httpPort
            host = "0.0.0.0"
        }

        // HTTPS not needed for heroku
//        sslConnector(
//            keyStore = loadKeyStore(),
//            keyAlias = "myalias",
//            keyStorePassword = { "password".toCharArray() },
//            privateKeyPassword = { "password".toCharArray() }
//        ) {
//            port = httpsPort
//            host = "0.0.0.0"
//        }
    }

    // Start the server with the environment
    embeddedServer(Netty, environment).start(wait = true)
}

fun Application.module() {
    configureContentNegotiation()
    configureCors()
    configureRouting()
}
