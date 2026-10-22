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

class LocalLlamaClient {
    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(120, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    suspend fun generateChat(
        endpointUrl: String,
        systemPrompt: String,
        history: List<Pair<String, String>>,
        userMessage: String
    ): Result<String> = withContext(Dispatchers.IO) {
        val targetUrl = endpointUrl.trim().ifBlank { "http://127.0.0.1:8080/v1/chat/completions" }

        try {
            val rootJson = JSONObject()
            val messagesArray = JSONArray()

            if (systemPrompt.isNotBlank()) {
                messagesArray.put(
                    JSONObject().apply {
                        put("role", "system")
                        put("content", systemPrompt)
                    }
                )
            }

            for ((role, text) in history) {
                val mappedRole = if (role == "assistant" || role == "model") "assistant" else "user"
                messagesArray.put(
                    JSONObject().apply {
                        put("role", mappedRole)
                        put("content", text)
                    }
                )
            }

            messagesArray.put(
                JSONObject().apply {
                    put("role", "user")
                    put("content", userMessage)
                }
            )

            rootJson.put("messages", messagesArray)
            rootJson.put("temperature", 0.7)
            rootJson.put("max_tokens", 800)
            rootJson.put("stream", false)

            val request = Request.Builder()
                .url(targetUrl)
                .addHeader("Content-Type", "application/json")
                .post(rootJson.toString().toRequestBody(jsonMediaType))
                .build()

            val response = client.newCall(request).execute()
            val responseBody = response.body?.string().orEmpty()

            if (!response.isSuccessful) {
                return@withContext Result.failure(
                    IOException("Local AI HTTP ${response.code}: ${responseBody.take(300)}")
                )
            }

            val respJson = JSONObject(responseBody)
            val choices = respJson.optJSONArray("choices")
            if (choices == null || choices.length() == 0) {
                return@withContext Result.failure(IOException("Local AI returned no choices."))
            }

            val messageObj = choices.getJSONObject(0).optJSONObject("message")
            val content = messageObj?.optString("content", "")?.trim().orEmpty()

            if (content.isBlank()) {
                return@withContext Result.failure(IOException("Local AI returned an empty answer."))
            }

            Result.success(content)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
