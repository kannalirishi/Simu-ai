package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "simu_facts")
data class SimuFact(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val fact: String,
    val createdAt: String
)

@Entity(tableName = "simu_notes")
data class SimuNote(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val note: String,
    val createdAt: String
)

@Entity(tableName = "simu_conversations")
data class SimuConversation(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val role: String, // "user", "assistant", "system"
    val message: String,
    val createdAt: String,
    val source: String = "ONLINE GEMINI" // "ONLINE GEMINI", "OFFLINE LOCAL", "APP LAUNCHER", "SYSTEM MEMORY"
)

@Entity(tableName = "simu_shortcuts")
data class SimuShortcut(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val label: String,
    val command: String,
    val packageName: String = ""
)
