package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.SimuConversation
import com.example.data.model.SimuFact
import com.example.data.model.SimuNote
import com.example.data.model.SimuShortcut
import kotlinx.coroutines.flow.Flow

@Dao
interface FactDao {
    @Query("SELECT * FROM simu_facts ORDER BY id DESC")
    fun getAllFacts(): Flow<List<SimuFact>>

    @Query("SELECT * FROM simu_facts ORDER BY id DESC LIMIT :limit")
    suspend fun getRecentFacts(limit: Int): List<SimuFact>

    @Query("SELECT * FROM simu_facts WHERE fact LIKE '%' || :query || '%' ORDER BY id DESC")
    suspend fun searchFacts(query: String): List<SimuFact>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFact(fact: SimuFact): Long

    @Query("DELETE FROM simu_facts WHERE id = :factId")
    suspend fun deleteFactById(factId: Long): Int

    @Delete
    suspend fun deleteFact(fact: SimuFact)
}

@Dao
interface NoteDao {
    @Query("SELECT * FROM simu_notes ORDER BY id DESC")
    fun getAllNotes(): Flow<List<SimuNote>>

    @Query("SELECT * FROM simu_notes ORDER BY id DESC LIMIT :limit")
    suspend fun getRecentNotes(limit: Int): List<SimuNote>

    @Query("SELECT * FROM simu_notes WHERE note LIKE '%' || :query || '%' ORDER BY id DESC")
    suspend fun searchNotes(query: String): List<SimuNote>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: SimuNote): Long

    @Query("DELETE FROM simu_notes WHERE id = :noteId")
    suspend fun deleteNoteById(noteId: Long): Int

    @Delete
    suspend fun deleteNote(note: SimuNote)
}

@Dao
interface ConversationDao {
    @Query("SELECT * FROM simu_conversations ORDER BY id ASC")
    fun getAllConversations(): Flow<List<SimuConversation>>

    @Query("SELECT * FROM simu_conversations ORDER BY id DESC LIMIT :limit")
    suspend fun getRecentMessages(limit: Int): List<SimuConversation>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConversation(item: SimuConversation): Long

    @Query("DELETE FROM simu_conversations")
    suspend fun clearAll()
}

@Dao
interface ShortcutDao {
    @Query("SELECT * FROM simu_shortcuts ORDER BY id ASC")
    fun getAllShortcuts(): Flow<List<SimuShortcut>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertShortcut(shortcut: SimuShortcut): Long

    @Query("DELETE FROM simu_shortcuts WHERE id = :id")
    suspend fun deleteShortcutById(id: Long): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(shortcuts: List<SimuShortcut>)
}
