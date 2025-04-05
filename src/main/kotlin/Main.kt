import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.http.content.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.routing.post
import job.JobManager
import kotlinx.serialization.json.Json
import model.*
import model.Constants.Routes.JOB_ID_PARAM


fun main() {
    val jobManager = JobManager()
    val httpPort = System.getenv(Constants.Env.HTTP_PORT)?.toInt() ?: 8080
    val httpsPort = System.getenv(Constants.Env.HTTPS_PORT)?.toInt() ?: 8443

    val environment = applicationEngineEnvironment {
        // Configure module
        module {
            install(ContentNegotiation) {
                json(Json { prettyPrint = true })
            }

            install(CORS) {
                allowHost("localhost:3000", schemes = listOf("http", "https"))
                allowMethod(HttpMethod.Options)
                allowMethod(HttpMethod.Put)
                allowMethod(HttpMethod.Delete)
                allowMethod(HttpMethod.Patch)
                allowHeader(HttpHeaders.Authorization)
                allowHeader(HttpHeaders.ContentType)
                allowCredentials = true
                maxAgeInSeconds = 3600
            }

            // Define your routes
            routing {

                staticResources("/", "static") {
                    // For SPAs, configure to default to index.html when path isn't found
                    default("index.html")
                }

                // Fallback route for SPA client-side routing
                get("{...}") {
                    call.resolveResource("static/index.html")?.let {
                        call.respond(it)
                    } ?: call.respondText("Not Found", status = HttpStatusCode.NotFound)
                }

                route("/api") {
                    post(Constants.Routes.SUBMIT_PROMPT) {
                        val request = call.receive<SubmitRequest>()
                        val job = jobManager.createJob(request.prompt)
                        call.respond(HttpStatusCode.Accepted, SubmitResponse(job.id))
                    }

                    get(Constants.Routes.RESULT_JOB_ID) {
                        val jobId = call.parameters[JOB_ID_PARAM]
                            ?: throw IllegalArgumentException("Missing job ID")
                        val job = jobManager.getJob(jobId)
                            ?: return@get call.respond(HttpStatusCode.NotFound)
                        call.respond(jobManager.getStatusResponse(job))
                    }

                    get(Constants.Routes.VOICES) {
                        val voiceInfoList = Voice.values().map {
                            VoiceInfo(key = it.name, displayName = it.toDisplayName())
                        }.toTypedArray()
                        call.respond(voiceInfoList)
                    }

                    get(Constants.Routes.HEALTH) {
                        call.respond(mapOf("status" to "OK"))
                    }
                }
            }
        }

        // Configure HTTP connector (optional)
        connector {
            port = httpPort
            host = "0.0.0.0"
        }

        // Configure HTTPS connector
        sslConnector(
            keyStore = loadKeyStore(),
            keyAlias = "myalias",
            keyStorePassword = { "password".toCharArray() },
            privateKeyPassword = { "password".toCharArray() }
        ) {
            port = httpsPort
            host = "0.0.0.0"
        }
    }

    // Start the server with the environment
    embeddedServer(Netty, environment).start(wait = true)
}

fun loadKeyStore(): java.security.KeyStore {
    val keyStore = java.security.KeyStore.getInstance("JKS")
    val fis = object {}.javaClass.getResourceAsStream("keystore/keystore.jks")
    keyStore.load(fis, "password".toCharArray())
    return keyStore
}