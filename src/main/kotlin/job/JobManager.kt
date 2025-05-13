package job

import client.GPTClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import model.*
import java.util.*
import java.util.concurrent.ConcurrentHashMap

/**
 * JobManager handles the lifecycle of text-to-speech conversion jobs. It provides functionality to create, track, and
 * process jobs asynchronously. Each job converts a text prompt to speech using OpenAI's API.
 */
class JobManager {
    private val jobs = ConcurrentHashMap<String, Job>()
    private val jobChannel = Channel<Job>(Channel.UNLIMITED)
    private val gptApiKey = System.getenv(Constants.Env.OPEN_AI_KEY)
        ?: throw IllegalStateException("Please ensure $${Constants.Env.OPEN_AI_KEY} environment variable is set")

    /**
     * Launches a coroutine that continuously processes jobs from the channel.
     */
    init {
        CoroutineScope(Dispatchers.Default).launch {
            for (job in jobChannel) {
                processJob(job)
            }
        }
    }

    /**
     * Creates a new job from a prompt submission request and queues it for processing.
     *
     * @param request Object containing the prompt text and voice configuration.
     * @return The created Job object with a unique ID and QUEUED status.
     */
    fun createJob(request: SubmitPromptBody): Job {
        val job = Job(
            id = UUID.randomUUID().toString(),
            prompt = request.prompt,
            voice = request.voice,
            status = JobStatus.QUEUED
        )
        jobs[job.id] = job
        jobChannel.trySend(job)
        return job
    }

    /**
     * Retrieves a job by its ID.
     *
     * @param id The unique identifier of the job.
     * @return The Job if found, or null if no job exists with the specified id.
     */
    fun getJob(id: String): Job? = jobs[id]

    /**
     * Generates a response object based on the current status of a job.
     *
     * @param job The job to generate a status response for.
     * @return A JobResponse containing status and appropriate result/error data.
     */
    fun getStatusResponse(job: Job): JobResponse {
        return when (job.status) {
            JobStatus.COMPLETED -> JobResponse(job.status, job.result)
            JobStatus.FAILED -> JobResponse(job.status, error = job.error)
            else -> JobResponse(status = job.status)
        }
    }

    /**
     * Processes a job and updates its status throughout its lifecycle.
     *
     * @param job The job to process.
     */
    private fun processJob(job: Job) {
        try {
            job.status = JobStatus.PROCESSING
            val result = promptForAudio(job)
            job.status = JobStatus.COMPLETED
            job.result = result
        } catch (e: Exception) {
            job.status = JobStatus.FAILED
            job.error = e.message
        }
    }

    /**
     * Generates audio from a text prompt using the OpenAI API.
     *
     * @param job The job containing the prompt and voice settings.
     * @return A [SpeechResult] object if successful, or null if text generation fails.
     */
    private fun promptForAudio(job: Job): SpeechResult? {
        val gptClient = GPTClient(gptApiKey)
        val response = gptClient.prompt(job.prompt)
        return response?.let { gptClient.textToSpeech(it, voice = job.voice) }
    }
}