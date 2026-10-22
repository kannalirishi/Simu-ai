package com.example.data.prefs

import android.content.Context
import android.content.SharedPreferences
import com.example.BuildConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SimuPreferences(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("simu_ai_preferences", Context.MODE_PRIVATE)

    data class Settings(
        val geminiApiKey: String,
        val geminiModel: String,
        val chatMode: String, // "auto", "online", "local"
        val localChatUrl: String,
        val autoSpeak: Boolean,
        val ttsPitch: Float,
        val ttsSpeed: Float,
        val userTitle: String,
        val voiceLanguage: String // "auto", "en", "te"
    )

    private val _settingsState = MutableStateFlow(loadSettings())
    val settingsState: StateFlow<Settings> = _settingsState.asStateFlow()

    fun getEffectiveApiKey(): String {
        val customKey = prefs.getString(KEY_GEMINI_API_KEY, "") ?: ""
        if (customKey.isNotBlank()) return customKey.trim()
        val buildKey = try {
            BuildConfig.GEMINI_API_KEY
        } catch (e: Throwable) {
            ""
        }
        if (buildKey == "MY_GEMINI_API_KEY") return ""
        return buildKey.trim()
    }

    private fun loadSettings(): Settings {
        val savedKey = prefs.getString(KEY_GEMINI_API_KEY, "") ?: ""
        val model = prefs.getString(KEY_GEMINI_MODEL, DEFAULT_MODEL) ?: DEFAULT_MODEL
        val mode = prefs.getString(KEY_CHAT_MODE, "auto") ?: "auto"
        val localUrl = prefs.getString(KEY_LOCAL_CHAT_URL, DEFAULT_LOCAL_URL) ?: DEFAULT_LOCAL_URL
        val autoSpeak = prefs.getBoolean(KEY_AUTO_SPEAK, true)
        val pitch = prefs.getFloat(KEY_TTS_PITCH, 1.0f)
        val speed = prefs.getFloat(KEY_TTS_SPEED, 1.0f)
        val title = prefs.getString(KEY_USER_TITLE, "Sir") ?: "Sir"
        val voiceLang = prefs.getString(KEY_VOICE_LANG, "auto") ?: "auto"

        return Settings(
            geminiApiKey = savedKey,
            geminiModel = model,
            chatMode = mode,
            localChatUrl = localUrl,
            autoSpeak = autoSpeak,
            ttsPitch = pitch,
            ttsSpeed = speed,
            userTitle = title,
            voiceLanguage = voiceLang
        )
    }

    fun updateSettings(
        geminiApiKey: String? = null,
        geminiModel: String? = null,
        chatMode: String? = null,
        localChatUrl: String? = null,
        autoSpeak: Boolean? = null,
        ttsPitch: Float? = null,
        ttsSpeed: Float? = null,
        userTitle: String? = null,
        voiceLanguage: String? = null
    ) {
        val editor = prefs.edit()
        geminiApiKey?.let { editor.putString(KEY_GEMINI_API_KEY, it) }
        geminiModel?.let { editor.putString(KEY_GEMINI_MODEL, it) }
        chatMode?.let { editor.putString(KEY_CHAT_MODE, it) }
        localChatUrl?.let { editor.putString(KEY_LOCAL_CHAT_URL, it) }
        autoSpeak?.let { editor.putBoolean(KEY_AUTO_SPEAK, it) }
        ttsPitch?.let { editor.putFloat(KEY_TTS_PITCH, it) }
        ttsSpeed?.let { editor.putFloat(KEY_TTS_SPEED, it) }
        userTitle?.let { editor.putString(KEY_USER_TITLE, it) }
        voiceLanguage?.let { editor.putString(KEY_VOICE_LANG, it) }
        editor.apply()

        _settingsState.value = loadSettings()
    }

    companion object {
        const val KEY_GEMINI_API_KEY = "gemini_api_key"
        const val KEY_GEMINI_MODEL = "gemini_model"
        const val KEY_CHAT_MODE = "chat_mode"
        const val KEY_LOCAL_CHAT_URL = "local_chat_url"
        const val KEY_AUTO_SPEAK = "auto_speak"
        const val KEY_TTS_PITCH = "tts_pitch"
        const val KEY_TTS_SPEED = "tts_speed"
        const val KEY_USER_TITLE = "user_title"
        const val KEY_VOICE_LANG = "voice_language"

        const val DEFAULT_MODEL = "gemini-2.5-flash"
        const val DEFAULT_LOCAL_URL = "http://127.0.0.1:8080/v1/chat/completions"

        val AVAILABLE_MODELS = listOf(
            "gemini-2.5-flash" to "Gemini 2.5 Flash (Ultra Fast & Smart - Recommended)",
            "gemini-2.5-pro" to "Gemini 2.5 Pro (Deep Reasoning)",
            "gemini-1.5-flash" to "Gemini 1.5 Flash (Lightweight)",
            "gemini-1.5-pro" to "Gemini 1.5 Pro (Standard Pro)",
            "custom" to "Custom Model Identifier"
        )
    }
}
