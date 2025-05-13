package application

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.http.content.*
import io.ktor.server.request.*
import io.ktor.server.resources.*
import io.ktor.server.resources.post
import io.ktor.server.response.*
import io.ktor.server.routing.*
import job.JobManager
import model.*

/**
 * Configures Routing for the Ktor application.
 */
fun Application.configureRouting() {
    val jobManager = JobManager()

    install(Resources)

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

            /**
             * Submits a request to create a job to generate audio for the provided prompt.
             *
             * @return a [SubmitPromptResponse] containing the [JobId] for the request.
             */
            post<SubmitPromptResponse.Submit> {
                val request = call.receive<SubmitPromptBody>()
                val job = jobManager.createJob(request)
                call.respond(HttpStatusCode.Accepted, SubmitPromptResponse(job.id))
            }

            /**
             * Fetches a [JobResponse] based on the [JobId] given in the request path parameters.
             *
             * @return [JobResponse] for the provided [JobId].
             */
            get<JobResponse.Fetch> {
                val jobId = call.parameters[Constants.Path.JOB_ID]
                    ?: throw IllegalArgumentException("Missing job ID")
                val job = jobManager.getJob(jobId)
                    ?: return@get call.respond(HttpStatusCode.NotFound)
                call.respond(jobManager.getStatusResponse(job))
            }

            /**
             * Fetches the [VoiceInfo] objects for all possible [Voice]s.
             *
             * @return an array of [VoiceInfo] objects.
             */
            get<VoiceInfo.Fetch> {
                val voiceInfoList = Voice.values().map {
                    VoiceInfo(key = it.name, displayName = it.toDisplayName())
                }.toTypedArray()
                call.respond(voiceInfoList)
            }
        }
    }
}