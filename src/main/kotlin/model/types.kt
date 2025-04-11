package model

import kotlinx.serialization.Serializable

typealias JobId = String

typealias Prompt = String

typealias SpeechResult = ByteArray

typealias Error = String

typealias DisplayName = String

typealias VoiceKey = String

@Serializable
enum class JobStatus { QUEUED, PROCESSING, COMPLETED, FAILED }

@Serializable
enum class Voice {
    ALLOY,
    ASH,
    BALLAD,
    CORAL,
    ECHO,
    FABLE,
    ONYX,
    NOVA,
    SAGE,
    SHIMMER;

    fun toDisplayName() = name.lowercase().replaceFirstChar { it.uppercase() }

    fun toRequestName() = name.lowercase()
}
