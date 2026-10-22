package com.example.data.network

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit

class GeminiApiClient {
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    suspend fun generateContent(
        apiKey: String,
        model: String,
        systemPrompt: String,
        history: List<Pair<String, String>>, // role ("user" | "assistant"), content
        userMessage: String
    ): Result<String> = withContext(Dispatchers.IO) {
        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext Result.failure(
                IllegalStateException("Gemini API key is not configured. Please enter your API key in Settings, Sir.")
            )
        }

        try {
            val rootJson = JSONObject()

            // System Instruction
            if (systemPrompt.isNotBlank()) {
                val sysParts = JSONArray().apply {
                    put(JSONObject().put("text", systemPrompt))
                }
                rootJson.put("systemInstruction", JSONObject().put("parts", sysParts))
            }

            // Contents array
            val contentsArray = JSONArray()
            for ((role, text) in history) {
                val geminiRole = if (role == "assistant" || role == "model") "model" else "user"
                val msgParts = JSONArray().apply {
                    put(JSONObject().put("text", text))
                }
                contentsArray.put(
                    JSONObject().apply {
                        put("role", geminiRole)
                        put("parts", msgParts)
                    }
                )
            }
            // Append current user message
            contentsArray.put(
                JSONObject().apply {
                    put("role", "user")
                    put("parts", JSONArray().apply { put(JSONObject().put("text", userMessage)) })
                }
            )
            rootJson.put("contents", contentsArray)

            // Generation config
            val genConfig = JSONObject().apply {
                put("temperature", 0.7)
                put("maxOutputTokens", 800)
            }
            rootJson.put("generationConfig", genConfig)

            val endpointModel = if (model.isBlank()) "gemini-2.5-flash" else model.trim()
            val url = "https://generativelanguage.googleapis.com/v1beta/models/$endpointModel:generateContent"

            val request = Request.Builder()
                .url(url)
                .addHeader("Content-Type", "application/json")
                .addHeader("x-goog-api-key", apiKey.trim())
                .post(rootJson.toString().toRequestBody(jsonMediaType))
                .build()

            val response = client.newCall(request).execute()
            val responseBody = response.body?.string().orEmpty()

            if (!response.isSuccessful) {
                val errorMsg = try {
                    val errJson = JSONObject(responseBody).optJSONObject("error")
                    errJson?.optString("message") ?: "HTTP ${response.code}: $responseBody"
                } catch (e: Exception) {
                    "HTTP ${response.code}: $responseBody"
                }
                return@withContext Result.failure(IOException("Gemini API error: $errorMsg"))
            }

            val respJson = JSONObject(responseBody)
            val candidates = respJson.optJSONArray("candidates")
            if (candidates == null || candidates.length() == 0) {
                return@withContext Result.failure(IOException("No response candidate returned by Gemini."))
            }

            val firstCandidate = candidates.getJSONObject(0)
            val content = firstCandidate.optJSONObject("content")
            val parts = content?.optJSONArray("parts")

            val textBuilder = StringBuilder()
            if (parts != null) {
                for (i in 0 until parts.length()) {
                    val partObj = parts.getJSONObject(i)
                    if (partObj.has("text")) {
                        textBuilder.append(partObj.getString("text"))
                    }
                }
            }

            val resultText = textBuilder.toString().trim()
            if (resultText.isBlank()) {
                return@withContext Result.failure(IOException("Gemini returned an empty message."))
            }

            Result.success(resultText)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
