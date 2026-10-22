package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.SimuDatabase
import com.example.data.model.SimuFact
import com.example.data.model.SimuNote
import com.example.data.prefs.SimuPreferences
import com.example.tools.CommandExecutionResult
import com.example.tools.CommandProcessor
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    private lateinit var database: SimuDatabase
    private lateinit var context: Context

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, SimuDatabase::class.java)
            .allowMainThreadQueries()
            .build()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun `read app_name from context`() {
        val appName = context.getString(R.string.app_name)
        assertEquals("SIMU AI", appName)
    }

    @Test
    fun `insert and retrieve memory fact in Room database`() = runBlocking {
        val factDao = database.factDao()
        val fact = SimuFact(fact = "Sir prefers dark futuristic consoles.", createdAt = "2026-10-22 12:00:00")
        val id = factDao.insertFact(fact)
        assertTrue(id > 0)

        val facts = factDao.getRecentFacts(10)
        assertEquals(1, facts.size)
        assertEquals("Sir prefers dark futuristic consoles.", facts[0].fact)
    }

    @Test
    fun `insert and retrieve note in Room database`() = runBlocking {
        val noteDao = database.noteDao()
        val note = SimuNote(note = "Flight ticket confirmed.", createdAt = "2026-10-22 12:00:00")
        val id = noteDao.insertNote(note)
        assertTrue(id > 0)

        val notes = noteDao.getRecentNotes(10)
        assertEquals(1, notes.size)
        assertEquals("Flight ticket confirmed.", notes[0].note)
    }

    @Test
    fun `command processor handles learn command`() = runBlocking {
        val prefs = SimuPreferences(context)
        val processor = CommandProcessor(
            database.factDao(),
            database.noteDao(),
            database.conversationDao(),
            prefs
        )

        val result = processor.processCommand(context, "learn my favorite color is neon cyan")
        assertTrue(result is CommandExecutionResult.Handled)
        val handled = result as CommandExecutionResult.Handled
        assertTrue(handled.message.contains("neon cyan"))

        val facts = database.factDao().getRecentFacts(10)
        assertEquals(1, facts.size)
        assertEquals("my favorite color is neon cyan", facts[0].fact)
    }
}
