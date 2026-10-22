package com.example.tools

import android.content.Context
import com.example.data.local.ConversationDao
import com.example.data.local.FactDao
import com.example.data.local.NoteDao
import com.example.data.model.SimuFact
import com.example.data.model.SimuNote
import com.example.data.prefs.SimuPreferences
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

sealed class CommandExecutionResult {
    data class Handled(val message: String, val source: String = "SYSTEM MEMORY") : CommandExecutionResult()
    data class AppLaunch(val appResult: AppLaunchResult) : CommandExecutionResult()
    object NotACommand : CommandExecutionResult()
}

class CommandProcessor(
    private val factDao: FactDao,
    private val noteDao: NoteDao,
    private val conversationDao: ConversationDao,
    private val preferences: SimuPreferences
) {
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

    suspend fun processCommand(context: Context, text: String): CommandExecutionResult {
        val raw = text.trim()
        val low = raw.lowercase(Locale.ROOT)
        val userTitle = preferences.settingsState.value.userTitle.ifBlank { "Sir" }
        val now = dateFormat.format(Date())

        // App Launch commands (e.g., "open youtube", "launch camera", "start whatsapp")
        if (low.startsWith("open ") || low.startsWith("launch ") || low.startsWith("start app ")) {
            val appResult = AppLauncherHelper.tryLaunchApp(context, raw, userTitle)
            return CommandExecutionResult.AppLaunch(appResult)
        }

        // Help
        if (low == "help" || low == "commands") {
            val help = """
                SIMU AI Commands Reference ($userTitle):
                • open <app>: Launch any app (e.g. open youtube, open camera)
                • learn <fact>: Store a fact in SQLite memory
                • memory: Display saved memory facts
                • forget <id>: Delete a saved fact by ID
                • note <text>: Save a personal note
                • notes: View your saved notes
                • search <text>: Search both memory facts and notes
                • mode <auto|online|offline>: Switch AI engine
                • voiceon / voiceoff: Toggle speech synthesis
                • speak <text>: Test speech synthesis
                • clearhistory: Clear chat messages (retains facts & notes)
            """.trimIndent()
            return CommandExecutionResult.Handled(help)
        }

        // Mode command
        if (low == "mode") {
            val cur = preferences.settingsState.value.chatMode
            return CommandExecutionResult.Handled("Current AI mode is: $cur, $userTitle.")
        }
        if (low.startsWith("mode ")) {
            val target = low.removePrefix("mode ").trim()
            val newMode = when (target) {
                "auto" -> "auto"
                "online" -> "online"
                "offline", "local" -> "local"
                else -> null
            }
            return if (newMode != null) {
                preferences.updateSettings(chatMode = newMode)
                CommandExecutionResult.Handled("SIMU AI mode set to '$newMode', $userTitle.")
            } else {
                CommandExecutionResult.Handled("Pardon, $userTitle. Please use: mode auto, mode online, or mode offline.")
            }
        }

        // Learn / Remember command
        if (low.startsWith("learn ")) {
            val factContent = raw.substring(6).trim()
            return if (factContent.isNotBlank()) {
                factDao.insertFact(SimuFact(fact = factContent, createdAt = now))
                CommandExecutionResult.Handled("Saved to local SQLite memory, $userTitle: \"$factContent\"")
            } else {
                CommandExecutionResult.Handled("Please specify the fact to remember, $userTitle (e.g., learn my favorite color is blue).")
            }
        }
        if (low.startsWith("remember that ") || low.startsWith("remember ")) {
            val prefix = if (low.startsWith("remember that ")) "remember that " else "remember "
            val factContent = raw.substring(prefix.length).trim()
            return if (factContent.isNotBlank()) {
                factDao.insertFact(SimuFact(fact = factContent, createdAt = now))
                CommandExecutionResult.Handled("Committed to long-term memory, $userTitle: \"$factContent\"")
            } else {
                CommandExecutionResult.Handled("What would you like me to remember, $userTitle?")
            }
        }

        // Memory command
        if (low == "memory" || low == "show memory" || low == "saved facts") {
            val facts = factDao.getRecentFacts(50)
            if (facts.isEmpty()) {
                return CommandExecutionResult.Handled("Your SQLite memory is currently empty, $userTitle. Use 'learn <fact>' to store details.")
            }
            val builder = StringBuilder("SIMU Local Memory ($userTitle):\n")
            facts.reversed().forEach {
                builder.append("#${it.id} [${it.createdAt}] ${it.fact}\n")
            }
            return CommandExecutionResult.Handled(builder.toString().trim())
        }

        // Forget command
        if (low.startsWith("forget ")) {
            val idStr = low.removePrefix("forget ").trim()
            val id = idStr.toLongOrNull()
            return if (id != null) {
                val deleted = factDao.deleteFactById(id)
                if (deleted > 0) {
                    CommandExecutionResult.Handled("Fact #$id erased from memory, $userTitle.")
                } else {
                    CommandExecutionResult.Handled("No fact with ID #$id was found in memory, $userTitle.")
                }
            } else {
                CommandExecutionResult.Handled("Usage: forget <numeric ID>, $userTitle.")
            }
        }

        // Note command
        if (low.startsWith("note ") || low.startsWith("take note ") || low.startsWith("add note ")) {
            val prefix = when {
                low.startsWith("take note ") -> "take note "
                low.startsWith("add note ") -> "add note "
                else -> "note "
            }
            val noteContent = raw.substring(prefix.length).trim()
            return if (noteContent.isNotBlank()) {
                noteDao.insertNote(SimuNote(note = noteContent, createdAt = now))
                CommandExecutionResult.Handled("Note recorded, $userTitle: \"$noteContent\"")
            } else {
                CommandExecutionResult.Handled("Please specify the text for the note, $userTitle.")
            }
        }

        // Notes command
        if (low == "notes" || low == "show notes") {
            val notes = noteDao.getRecentNotes(50)
            if (notes.isEmpty()) {
                return CommandExecutionResult.Handled("No saved notes found, $userTitle. Use 'note <text>' to record one.")
            }
            val builder = StringBuilder("Saved Notes ($userTitle):\n")
            notes.reversed().forEach {
                builder.append("• [${it.createdAt}] ${it.note}\n")
            }
            return CommandExecutionResult.Handled(builder.toString().trim())
        }

        // Search command
        if (low.startsWith("search ")) {
            val query = raw.substring(7).trim()
            if (query.isBlank()) {
                return CommandExecutionResult.Handled("Please specify a search query, $userTitle.")
            }
            val facts = factDao.searchFacts(query)
            val notes = noteDao.searchNotes(query)

            if (facts.isEmpty() && notes.isEmpty()) {
                return CommandExecutionResult.Handled("No matching facts or notes found for '$query', $userTitle.")
            }

            val builder = StringBuilder("Search results for '$query' ($userTitle):\n\n")
            if (facts.isNotEmpty()) {
                builder.append("Facts:\n")
                facts.forEach { builder.append("  • #${it.id}: ${it.fact}\n") }
                builder.append("\n")
            }
            if (notes.isNotEmpty()) {
                builder.append("Notes:\n")
                notes.forEach { builder.append("  • #${it.id}: ${it.note}\n") }
            }
            return CommandExecutionResult.Handled(builder.toString().trim())
        }

        // Voice toggle commands
        if (low == "voiceon" || low == "voice on") {
            preferences.updateSettings(autoSpeak = true)
            return CommandExecutionResult.Handled("Voice synthesis is now active, $userTitle.")
        }
        if (low == "voiceoff" || low == "voice off") {
            preferences.updateSettings(autoSpeak = false)
            return CommandExecutionResult.Handled("Voice synthesis disabled, $userTitle.")
        }

        // Clear history command
        if (low == "clearhistory" || low == "clear history") {
            conversationDao.clearAll()
            return CommandExecutionResult.Handled("Conversation history has been cleared, $userTitle. Facts and notes remain safe.")
        }

        // Who are you / status
        if (low == "who are you" || low == "status" || low == "system status") {
            val s = preferences.settingsState.value
            val factCount = factDao.getRecentFacts(100).size
            val statusMsg = """
                SIMU AI Status Report:
                • Master: $userTitle
                • Mode: ${s.chatMode.uppercase()}
                • Online Model: ${s.geminiModel}
                • Voice Output: ${if (s.autoSpeak) "Enabled" else "Muted"}
                • Stored Memory Facts: $factCount
                • Local DB: simu_memory.db (Active)
            """.trimIndent()
            return CommandExecutionResult.Handled(statusMsg)
        }

        return CommandExecutionResult.NotACommand
    }
}
