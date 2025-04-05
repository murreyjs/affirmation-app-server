package model

import kotlinx.serialization.Serializable

@Serializable
data class SubmitRequest(
    val prompt: Prompt,
    val voice: Voice,
)

@Serializable
data class SubmitResponse(val jobId: JobId)

@Serializable
data class StatusResponse(
    val status: JobStatus,
    val result: Result? = null,
    val error: Error? = null,
)

@Serializable
data class VoiceInfo(
    val key: VoiceKey,
    val displayName: DisplayName,
)

data class Job(
    val id: JobId,
    val prompt: Prompt,
    var status: JobStatus,
    var result: Result? = null,
    var error: Error? = null
)