package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.SimuDatabase
import com.example.data.model.SimuConversation
import com.example.data.model.SimuFact
import com.example.data.model.SimuNote
import com.example.data.model.SimuShortcut
import com.example.data.network.AiRepository
import com.example.data.network.GeminiApiClient
import com.example.data.network.LocalLlamaClient
import com.example.data.prefs.SimuPreferences
import com.example.tools.AppLauncherHelper
import com.example.tools.CommandExecutionResult
import com.example.tools.CommandProcessor
import com.example.tools.InstalledAppItem
import com.example.voice.VoiceManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class SimuTab(val label: String) {
    CHAT("Console"),
    MEMORY("Memory"),
    NOTES("Notes"),
    APPS("App Launcher"),
    SETTINGS("Settings")
}

class SimuViewModel(application: Application) : AndroidViewModel(application) {

    private val database = SimuDatabase.getDatabase(application)
    private val factDao = database.factDao()
    private val noteDao = database.noteDao()
    private val conversationDao = database.conversationDao()
    private val shortcutDao = database.shortcutDao()

    val preferences = SimuPreferences(application)
    val settingsState: StateFlow<SimuPreferences.Settings> = preferences.settingsState

    private val geminiClient = GeminiApiClient()
    private val localLlamaClient = LocalLlamaClient()
    private val aiRepository = AiRepository(
        geminiClient = geminiClient,
        localLlamaClient = localLlamaClient,
        factDao = factDao,
        noteDao = noteDao,
        conversationDao = conversationDao,
        preferences = preferences
    )

    private val commandProcessor = CommandProcessor(
        factDao = factDao,
        noteDao = noteDao,
        conversationDao = conversationDao,
        preferences = preferences
    )

    val voiceManager = VoiceManager(application)

    val conversations: StateFlow<List<SimuConversation>> = conversationDao.getAllConversations()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val facts: StateFlow<List<SimuFact>> = factDao.getAllFacts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val notes: StateFlow<List<SimuNote>> = noteDao.getAllNotes()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val shortcuts: StateFlow<List<SimuShortcut>> = shortcutDao.getAllShortcuts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _currentTab = MutableStateFlow(SimuTab.CHAT)
    val currentTab: StateFlow<SimuTab> = _currentTab.asStateFlow()

    private val _isProcessing = MutableStateFlow(false)
    val isProcessing: StateFlow<Boolean> = _isProcessing.asStateFlow()

    private val _inputText = MutableStateFlow("")
    val inputText: StateFlow<String> = _inputText.asStateFlow()

    private val _toastEvent = MutableSharedFlow<String>()
    val toastEvent: SharedFlow<String> = _toastEvent.asSharedFlow()

    private val _installedApps = MutableStateFlow<List<InstalledAppItem>>(emptyList())
    val installedApps: StateFlow<List<InstalledAppItem>> = _installedApps.asStateFlow()

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

    init {
        loadInstalledApps()
    }

    fun selectTab(tab: SimuTab) {
        _currentTab.value = tab
    }

    fun updateInputText(newText: String) {
        _inputText.value = newText
    }

    fun loadInstalledApps() {
        viewModelScope.launch(Dispatchers.IO) {
            val apps = AppLauncherHelper.getInstalledLaunchableApps(getApplication())
            _installedApps.value = apps
        }
    }

    fun submitQuery(context: Context, text: String = _inputText.value) {
        val query = text.trim()
        if (query.isBlank() || _isProcessing.value) return

        _inputText.value = ""
        val now = dateFormat.format(Date())

        viewModelScope.launch(Dispatchers.IO) {
            // Save user message in Room
            conversationDao.insertConversation(
                SimuConversation(
                    role = "user",
                    message = query,
                    createdAt = now,
                    source = "USER"
                )
            )

            _isProcessing.value = true

            // Check if it is a command or app launcher intent
            val commandResult = commandProcessor.processCommand(context, query)
            val replyMessage: String
            val sourceBadge: String

            when (commandResult) {
                is CommandExecutionResult.Handled -> {
                    replyMessage = commandResult.message
                    sourceBadge = commandResult.source
                }
                is CommandExecutionResult.AppLaunch -> {
                    replyMessage = commandResult.appResult.message
                    sourceBadge = "APP LAUNCHER"
                }
                is CommandExecutionResult.NotACommand -> {
                    // Send to AI Repository (Gemini / Local Qwen / Offline)
                    val aiResponse = aiRepository.generateAnswer(query)
                    replyMessage = aiResponse.reply
                    sourceBadge = aiResponse.source
                }
            }

            // Save assistant response
            val respTime = dateFormat.format(Date())
            conversationDao.insertConversation(
                SimuConversation(
                    role = "assistant",
                    message = replyMessage,
                    createdAt = respTime,
                    source = sourceBadge
                )
            )

            _isProcessing.value = false

            // Voice speak if enabled
            val settings = preferences.settingsState.value
            if (settings.autoSpeak) {
                voiceManager.speak(
                    text = replyMessage,
                    pitch = settings.ttsPitch,
                    speed = settings.ttsSpeed,
                    preferredLanguage = settings.voiceLanguage
                )
            }
        }
    }

    fun toggleVoiceInput(context: Context, hasRecordAudioPermission: Boolean) {
        if (!hasRecordAudioPermission) {
            viewModelScope.launch {
                _toastEvent.emit("Microphone permission is required for voice input.")
            }
            return
        }

        if (voiceManager.isListening.value) {
            voiceManager.stopListening()
        } else {
            voiceManager.startListening(
                onResult = { spokenText ->
                    if (spokenText.isNotBlank()) {
                        submitQuery(context, spokenText)
                    }
                },
                onError = { err ->
                    viewModelScope.launch {
                        _toastEvent.emit("Voice recognition: $err")
                    }
                }
            )
        }
    }

    fun speak(text: String) {
        val s = preferences.settingsState.value
        voiceManager.speak(
            text = text,
            pitch = s.ttsPitch,
            speed = s.ttsSpeed,
            preferredLanguage = s.voiceLanguage
        )
    }

    fun stopSpeaking() {
        voiceManager.stopSpeaking()
    }

    fun addFact(factText: String) {
        if (factText.isBlank()) return
        val now = dateFormat.format(Date())
        viewModelScope.launch(Dispatchers.IO) {
            factDao.insertFact(SimuFact(fact = factText.trim(), createdAt = now))
            _toastEvent.emit("Fact committed to memory, Sir.")
        }
    }

    fun deleteFact(id: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            factDao.deleteFactById(id)
            _toastEvent.emit("Fact #$id erased.")
        }
    }

    fun addNote(noteText: String) {
        if (noteText.isBlank()) return
        val now = dateFormat.format(Date())
        viewModelScope.launch(Dispatchers.IO) {
            noteDao.insertNote(SimuNote(note = noteText.trim(), createdAt = now))
            _toastEvent.emit("Note saved successfully, Sir.")
        }
    }

    fun deleteNote(id: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            noteDao.deleteNoteById(id)
            _toastEvent.emit("Note deleted.")
        }
    }

    fun launchAppFromGrid(context: Context, app: InstalledAppItem) {
        val title = preferences.settingsState.value.userTitle.ifBlank { "Sir" }
        val result = AppLauncherHelper.tryLaunchApp(context, app.label, title)
        val now = dateFormat.format(Date())
        viewModelScope.launch(Dispatchers.IO) {
            conversationDao.insertConversation(
                SimuConversation(
                    role = "user",
                    message = "Open ${app.label}",
                    createdAt = now,
                    source = "USER"
                )
            )
            conversationDao.insertConversation(
                SimuConversation(
                    role = "assistant",
                    message = result.message,
                    createdAt = now,
                    source = "APP LAUNCHER"
                )
            )
            if (preferences.settingsState.value.autoSpeak) {
                speak(result.message)
            }
        }
    }

    fun clearHistory() {
        viewModelScope.launch(Dispatchers.IO) {
            conversationDao.clearAll()
            _toastEvent.emit("Conversation history cleared.")
        }
    }

    override fun onCleared() {
        super.onCleared()
        voiceManager.destroy()
    }
}
