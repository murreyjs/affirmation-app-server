package client

import io.ktor.server.plugins.*
import model.Constants
import model.Prompt
import model.SpeechResult
import model.Voice
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

class GPTClient(private val apiKey: String) {
    private val client = OkHttpClient()

    companion object {
        private const val APPLICATION_JSON = "application/json"
        private const val AUTHORIZATION = "Authorization"
        private const val BEARER = "Bearer"
        private const val CHARSET_UTF_8 = "charset=utf-8"
        private const val CHOICES = "choices"
        private const val CONTENT = "content"
        private const val CONTENT_TYPE = "Content-Type"
        private const val INPUT = "input"
        private const val MESSAGE = "message"
        private const val MESSAGES = "messages"
        private const val MODEL = "model"
        private const val MP3 = "mp3"
        private const val RESPONSE_FORMAT = "response_format"
        private const val ROLE = "role"
        private const val SPEED = "speed"
        private const val SYSTEM = "system"
        private const val USER = "user"
        private const val VOICE = "voice"
    }

    fun prompt(prompt: Prompt): String? {
        val requestBody = buildPromptRequestBody(prompt)
        val request = buildPromptRequest(requestBody)
        val responseObj = client.newCall(request).execute()

        return if (responseObj.isSuccessful) {
            val responseBody = responseObj.body?.string()
            val jsonResponse = JSONObject(responseBody)
            val choices = jsonResponse.getJSONArray(CHOICES)
            if (choices.length() > 0) {
                choices.getJSONObject(0).getJSONObject(MESSAGE).getString(CONTENT)
            } else {
                throw IllegalStateException("No response given for prompt")
            }
        } else {
            throw BadRequestException("OpenAI API request failed: ${responseObj.code} - ${responseObj.message}")
        }
    }

    fun textToSpeech(
        text: String,
        model: String = Constants.Models.TTS_1,
        voice: Voice = Voice.ALLOY,
        outputFormat: String = MP3,
        speed: Double = 1.0,
    ): SpeechResult {
        val requestBody = buildTextToSpeechRequestBody(model, text, voice, outputFormat, speed)
        val request = buildTextToSpeechRequest(requestBody)
        val response = client.newCall(request).execute()

        if (!response.isSuccessful) {
            throw BadRequestException("OpenAI API request failed: ${response.code} - ${response.message}")
        }

        return response.body?.bytes()
            ?: throw Exception("Response body is empty")
    }

    private fun buildPromptRequestBody(prompt: Prompt): RequestBody {
        return JSONObject()
            .put(MODEL, Constants.Models.GPT_4O_MINI)
            .put(MESSAGES, listOf(
                mapOf(ROLE to SYSTEM, CONTENT to "You are a meditation generator. Always respond with ONLY the meditation text itself, without any introduction, explanation, or conclusion. Never prefix your response with phrases like 'Here is a meditation:' or similar text. Begin speaking directly as if guiding the meditation."),
                mapOf(ROLE to USER, CONTENT to prompt)
            ))
            .toString()
            .toRequestBody(APPLICATION_JSON.toMediaTypeOrNull())
    }

    private fun buildTextToSpeechRequestBody(
        model: String,
        text: String,
        voice: Voice,
        outputFormat: String,
        speed: Double
    ): RequestBody {
        return JSONObject().apply {
            put(MODEL, model)
            put(INPUT, text)
            put(VOICE, voice.toRequestName())
            put(RESPONSE_FORMAT, outputFormat)
            put(SPEED, speed)
        }
        .toString()
        .toRequestBody("$APPLICATION_JSON; $CHARSET_UTF_8".toMediaType())
    }

    private fun buildPromptRequest(requestBody: RequestBody): Request {
        return  Request.Builder().url(Constants.Requests.OPEN_AI_COMPLETIONS)
            .addHeader(CONTENT_TYPE, APPLICATION_JSON)
            .addHeader(AUTHORIZATION, "$BEARER $apiKey")
            .post(requestBody).build()
    }

    private fun buildTextToSpeechRequest(requestBody: RequestBody): Request {
        return Request.Builder()
            .url(Constants.Requests.OPEN_AI_TTS)
            .addHeader(AUTHORIZATION, "$BEARER $apiKey")
            .post(requestBody)
            .build()
    }
}