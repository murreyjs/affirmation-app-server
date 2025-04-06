package model

object Constants {

    object Routes {
        const val JOB_ID_PARAM = "jobId"
        const val SUBMIT_PROMPT = "/submit-prompt"
        const val RESULT_JOB_ID = "/result/{$JOB_ID_PARAM}"
        const val HEALTH = "/health"
        const val VOICES = "/voices"
    }

    object Env {
        const val OPEN_AI_KEY = "openai.key"
        const val HTTP_PORT = "port.http"
        const val HTTPS_PORT = "port.https"
    }

    object Requests {
        const val OPEN_AI_COMPLETIONS = "https://api.openai.com/v1/chat/completions"
        const val OPEN_AI_TTS = "https://api.openai.com/v1/audio/speech"
    }

    object Models {
        const val GPT_4O_MINI = "gpt-4o-mini"
        const val TTS_1 = "tts-1"
    }
}