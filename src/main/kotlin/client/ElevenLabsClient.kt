package client

import io.ktor.server.plugins.*
import model.SpeechResult
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

class ElevenLabsClient(private val apiKey: String) {
    private val client = OkHttpClient()

    fun textToSpeech(
        text: String,
        voiceId: String,

    ): SpeechResult {
        val jsonPayload = JSONObject().apply {
            put("text", text)
            put("model_id", "eleven_monolingual_v1")
            put("voice_settings", JSONObject().apply {
                put("stability", 0.5)
                put("similarity_boost", 0.5)
            })
        }

        val mediaType = "application/json; charset=utf-8".toMediaType()
        val requestBody = jsonPayload.toString().toRequestBody(mediaType)
        val request = Request.Builder()
            .url("https://api.elevenlabs.io/v1/text-to-speech/$voiceId")
            .post(requestBody)
            .addHeader("xi-api-key", apiKey)
            .addHeader("Content-Type", "application/json")
            .build()
        val response = client.newCall(request).execute()

        if (!response.isSuccessful) {
            throw BadRequestException("OpenAI API request failed: ${response.code} - ${response.message}")
        }

        return response.body?.bytes()
            ?: throw Exception("Response body is empty")
    }
}
