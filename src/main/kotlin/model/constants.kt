package model

/**
 * Object containing constant values used throughout the application.
 */
object Constants {

    /**
     * Constants for path parameters.
     */
    object Path {
        const val JOB_ID = "jobId"
    }

    /**
     * Constants for environment variables.
     */
    object Env {
        const val OPEN_AI_KEY = "OPEN_AI_KEY"
        const val PORT = "PORT"
    }

    /**
     * Constants for URLs used in requests.
     */
    object Requests {
        const val OPEN_AI_COMPLETIONS = "https://api.openai.com/v1/chat/completions"
        const val OPEN_AI_TTS = "https://api.openai.com/v1/audio/speech"
    }

    /**
     * Constants for Open AI model names.
     */
    object Models {
        const val GPT_4O_MINI = "gpt-4o-mini"
        const val TTS_1 = "tts-1"
    }
}