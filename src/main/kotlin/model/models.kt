package model

import io.ktor.resources.*
import kotlinx.serialization.Serializable

@Serializable
data class SubmitPromptResponse(val jobId: JobId) {

    @Resource("/submit-prompt")
    class Submit
}

@Serializable
data class SubmitPromptBody(
    val prompt: Prompt,
    val voice: Voice,
)

@Serializable
data class JobResponse(
    val status: JobStatus,
    val result: SpeechResult? = null,
    val error: Error? = null,
) {

    @Resource("/job/{${Constants.Path.JOB_ID}}")
    class Fetch(val jobId: JobId)
}

@Serializable
data class VoiceInfo(
    val key: VoiceKey,
    val displayName: DisplayName,
) {

    @Resource("/voices")
    class Fetch
}

data class Job(
    val id: JobId,
    val prompt: Prompt,
    val voice: Voice,
    var status: JobStatus,
    var result: SpeechResult? = null,
    var error: Error? = null
)