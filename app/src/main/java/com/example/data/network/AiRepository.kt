package com.example.data.network

import com.example.data.local.ConversationDao
import com.example.data.local.FactDao
import com.example.data.local.NoteDao
import com.example.data.prefs.SimuPreferences

data class AiResponse(
    val reply: String,
    val source: String // "ONLINE GEMINI", "OFFLINE LOCAL", "SIMU OFFLINE"
)

class AiRepository(
    private val geminiClient: GeminiApiClient,
    private val localLlamaClient: LocalLlamaClient,
    private val factDao: FactDao,
    private val noteDao: NoteDao,
    private val conversationDao: ConversationDao,
    private val preferences: SimuPreferences
) {
    suspend fun generateAnswer(userMessage: String): AiResponse {
        val settings = preferences.settingsState.value
        val effectiveApiKey = preferences.getEffectiveApiKey()
        val userTitle = settings.userTitle.ifBlank { "Sir" }

        // Fetch saved memory facts to ground the assistant
        val savedFacts = factDao.getRecentFacts(12)
        val memoryBlock = if (savedFacts.isEmpty()) {
            "(No saved facts yet in local SQLite database.)"
        } else {
            savedFacts.joinToString("\n") { "- ${it.fact}" }
        }

        val systemPrompt = """
            You are SIMU, a highly loyal, polite, and intelligent personal AI assistant.
            Always address the user respectfully as "$userTitle" (e.g., "Right away, $userTitle", "As you wish, $userTitle", "Understood, $userTitle").
            The user may communicate in English or Telugu; reply in the language they use when possible.
            Answer clearly, concisely, naturally, and honestly.
            Use the saved memory facts below only when relevant:
            $memoryBlock
            
            Keep responses helpful, polite, and actionable.
        """.trimIndent()

        // Fetch recent conversation history
        val recentHistory = conversationDao.getRecentMessages(10).reversed().map {
            it.role to it.message
        }

        val mode = settings.chatMode.lowercase()

        // Local only mode
        if (mode == "local" || mode == "offline") {
            val localResult = localLlamaClient.generateChat(
                endpointUrl = settings.localChatUrl,
                systemPrompt = systemPrompt,
                history = recentHistory,
                userMessage = userMessage
            )
            return if (localResult.isSuccess) {
                AiResponse(localResult.getOrThrow(), "OFFLINE LOCAL")
            } else {
                AiResponse(
                    generateOfflineFallback(userMessage, userTitle, "Local server connection failed: ${localResult.exceptionOrNull()?.message}"),
                    "SIMU OFFLINE"
                )
            }
        }

        // Online only mode
        if (mode == "online") {
            val geminiResult = geminiClient.generateContent(
                apiKey = effectiveApiKey,
                model = settings.geminiModel,
                systemPrompt = systemPrompt,
                history = recentHistory,
                userMessage = userMessage
            )
            return if (geminiResult.isSuccess) {
                AiResponse(geminiResult.getOrThrow(), "ONLINE GEMINI")
            } else {
                val err = geminiResult.exceptionOrNull()?.message ?: "Unknown error"
                AiResponse(
                    "Pardon me, $userTitle. Online Gemini service encountered an error: $err. Please check your API key and model in Settings.",
                    "ONLINE GEMINI"
                )
            }
        }

        // Auto mode: Try Gemini first, fall back to local llama.cpp, fall back to smart offline response
        val onlineResult = geminiClient.generateContent(
            apiKey = effectiveApiKey,
            model = settings.geminiModel,
            systemPrompt = systemPrompt,
            history = recentHistory,
            userMessage = userMessage
        )

        if (onlineResult.isSuccess) {
            return AiResponse(onlineResult.getOrThrow(), "ONLINE GEMINI")
        }

        // Attempt local server fallback
        val localResult = localLlamaClient.generateChat(
            endpointUrl = settings.localChatUrl,
            systemPrompt = systemPrompt,
            history = recentHistory,
            userMessage = userMessage
        )

        if (localResult.isSuccess) {
            return AiResponse(localResult.getOrThrow(), "OFFLINE LOCAL")
        }

        // Both remote/local models unavailable; provide polite on-device response
        val onlineErr = onlineResult.exceptionOrNull()?.message ?: "Gemini unavailable"
        val fallbackText = generateOfflineFallback(userMessage, userTitle, onlineErr)
        return AiResponse(fallbackText, "SIMU OFFLINE")
    }

    private suspend fun generateOfflineFallback(userMessage: String, userTitle: String, note: String): String {
        val lower = userMessage.trim().lowercase()

        // Check if querying memory or notes
        if (lower.contains("who am i") || lower.contains("my name") || lower.contains("remember")) {
            val facts = factDao.getRecentFacts(5)
            if (facts.isNotEmpty()) {
                val list = facts.joinToString("; ") { it.fact }
                return "From your local SQLite memory, $userTitle: $list."
            }
        }

        if (lower.contains("hello") || lower.contains("hi") || lower.contains("hey")) {
            return "Good day, $userTitle! I am SIMU. While remote AI is currently unavailable ($note), my local SQLite memory, notes, and app launcher remain fully operational."
        }

        if (lower.contains("how are you")) {
            return "All local systems are operating at peak efficiency, $userTitle. How may I be of service?"
        }

        if (lower.contains("time") || lower.contains("date")) {
            val now = java.text.SimpleDateFormat("EEEE, MMMM d, yyyy 'at' h:mm a", java.util.Locale.getDefault()).format(java.util.Date())
            return "The current local time is $now, $userTitle."
        }

        return "Understood, $userTitle. I have logged your request into local storage. (Note: Online Gemini is unreachable ($note). You can configure your Gemini API Key or local llama server in Settings.)"
    }
}
