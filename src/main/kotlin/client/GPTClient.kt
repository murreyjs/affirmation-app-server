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

/**
 * Client for interacting with OpenAI's GPT API services.
 *
 * @property apiKey The OpenAI API key used for authentication.
 */
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

    /**
     * Sends a prompt to the OpenAI API and returns the generated response content.
     *
     * This method communicates with the OpenAI API to generate a response based on the provided prompt.
     * It builds the request, executes it, and processes the API response to extract the content.
     *
     * @param prompt The [Prompt] containing the text to send to the OpenAI API.
     *
     * @return The generated content string from the API response, or null if no content is available.
     *
     * @throws IllegalStateException If the API response contains no choices.
     * @throws BadRequestException If the API request fails with an error code.
     */
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

    /**
     * Converts text to speech using the OpenAI Text-to-Speech API.
     *
     * This method takes a text input and various configuration parameters to generate
     * an audio file containing the synthesized speech.
     *
     * @param text The text content to convert to speech.
     * @param model The OpenAI TTS model to use, defaults to TTS_1.
     * @param voice The voice type to use for speech synthesis, defaults to ALLOY.
     * @param outputFormat The audio format for the output file, defaults to MP3.
     * @param speed The playback speed of the generated audio, defaults to 1.0.
     *
     * @return [SpeechResult] containing the binary audio data.
     *
     * @throws BadRequestException If the API request fails with an error code.
     * @throws Exception If the response body is empty.
     */
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

    /**
     * Builds the request body for a prompt API call.
     *
     * Creates a JSON object containing the model specification and message array
     * for communication with the OpenAI Completions API. The system message configures
     * the AI to respond as a meditation generator.
     *
     * @param prompt The Prompt object containing the user's input.
     *
     * @return RequestBody object containing the formatted JSON request.
     */
    private fun buildPromptRequestBody(prompt: Prompt): RequestBody {
        return JSONObject()
            .put(MODEL, Constants.Models.GPT_4O_MINI)
            .put(MESSAGES, listOf(
                mapOf(ROLE to SYSTEM, CONTENT to "You are a meditation generator. Always respond with ONLY the " +
                    "meditation text itself, without any introduction, explanation, or conclusion. Never prefix " +
                    "your response with phrases like 'Here is a meditation:' or similar text. Begin speaking " +
                    "directly as if guiding the meditation."),
                mapOf(ROLE to USER, CONTENT to prompt)
            ))
            .toString()
            .toRequestBody(APPLICATION_JSON.toMediaTypeOrNull())
    }

    /**
     * Builds the request body for a text-to-speech API call.
     *
     * Creates a JSON object with the necessary parameters for the OpenAI TTS API,
     * including the model, input text, voice selection, output format, and playback speed.
     *
     * @param model The OpenAI TTS model identifier.
     * @param text The text to convert to speech.
     * @param voice The voice type to use for synthesis.
     * @param outputFormat The desired audio format for the output.
     * @param speed The playback speed multiplier.
     *
     * @return RequestBody object containing the formatted JSON request.
     */
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

    /**
     * Builds an HTTP request for the OpenAI Completions API.
     *
     * Creates a Request object with the appropriate URL, headers (including API authentication),
     * and request body for communicating with the OpenAI Completions endpoint.
     *
     * @param requestBody The JSON request body to send.
     *
     * @return Request object configured for the OpenAI Completions API.
     */
    private fun buildPromptRequest(requestBody: RequestBody): Request {
        return  Request.Builder().url(Constants.Requests.OPEN_AI_COMPLETIONS)
            .addHeader(CONTENT_TYPE, APPLICATION_JSON)
            .addHeader(AUTHORIZATION, "$BEARER $apiKey")
            .post(requestBody).build()
    }

    /**
     * Builds an HTTP request for the OpenAI Text-to-Speech API.
     *
     * Creates a Request object with the appropriate URL, headers (including API authentication),
     * and request body for communicating with the OpenAI TTS endpoint.
     *
     * @param requestBody The JSON request body to send.
     *
     * @return Request object configured for the OpenAI TTS API.
     */
    private fun buildTextToSpeechRequest(requestBody: RequestBody): Request {
        return Request.Builder()
            .url(Constants.Requests.OPEN_AI_TTS)
            .addHeader(AUTHORIZATION, "$BEARER $apiKey")
            .post(requestBody)
            .build()
    }
}