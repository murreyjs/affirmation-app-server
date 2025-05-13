package model

import kotlinx.serialization.Serializable

/**
 * Type alias for a [Job] ID as a [String].
 */
typealias JobId = String

/**
 * Type alias for a prompt as a [String].
 */
typealias Prompt = String

/**
 * Type alias for the result of text to speech as a [ByteArray].
 */
typealias SpeechResult = ByteArray

/**
 * Type alias for the text of an error as a [String].
 */
typealias Error = String

/**
 * Type alias for a display name as a [String].
 */
typealias DisplayName = String

/**
 * Type alias for a VoiceKey as a [String].
 */
typealias VoiceKey = String

/**
 * Enum for the status of a [Job].
 */
@Serializable
enum class JobStatus { QUEUED, PROCESSING, COMPLETED, FAILED }

/**
 * Enum for all possible values for the voice parameter to an Open AI text to speech request.
 */
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

    /**
     * Converts the [Voice] enum name to a name that can be displayed in the UI.
     */
    fun toDisplayName() = name.lowercase().replaceFirstChar { it.uppercase() }

    /**
     * Converts the [Voice] enum name to the name used in the request to Open AI.
     */
    fun toRequestName() = name.lowercase()
}
