package job

import client.ElevenLabsClient
import client.GPTClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import model.*
import java.util.*
import java.util.concurrent.ConcurrentHashMap

class JobManager {
    private val jobs = ConcurrentHashMap<String, Job>()
    private val jobChannel = Channel<Job>(Channel.UNLIMITED)
    private val gptApiKey = System.getenv(Constants.Env.OPEN_AI_KEY)
        ?: throw IllegalStateException("Please ensure $${Constants.Env.OPEN_AI_KEY} environment variable is set")
    private val labsKey = System.getenv(Constants.Env.ELEVEN_LABS_KEY)
        ?: throw IllegalStateException("Please ensure $${Constants.Env.ELEVEN_LABS_KEY} environment variable is set")

    init {
        CoroutineScope(Dispatchers.Default).launch {
            for (job in jobChannel) {
                processJob(job)
            }
        }
    }

    fun createJob(request: SubmitRequest): Job {
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

    fun getJob(id: String): Job? = jobs[id]

    fun getStatusResponse(job: Job): StatusResponse {
        return when (job.status) {
            JobStatus.COMPLETED -> StatusResponse(job.status, job.result)
            JobStatus.FAILED -> StatusResponse(job.status, error = job.error)
            else -> StatusResponse(status = job.status)
        }
    }

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

    private fun promptForAudio(job: Job): SpeechResult? {
        val gptClient = GPTClient(gptApiKey)
        val response = gptClient.prompt(job.prompt)
        return response?.let { gptClient.textToSpeech(it, voice = job.voice) }
    }
}