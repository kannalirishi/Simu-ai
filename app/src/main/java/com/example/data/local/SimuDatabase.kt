package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.SimuConversation
import com.example.data.model.SimuFact
import com.example.data.model.SimuNote
import com.example.data.model.SimuShortcut
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Database(
    entities = [SimuFact::class, SimuNote::class, SimuConversation::class, SimuShortcut::class],
    version = 1,
    exportSchema = false
)
abstract class SimuDatabase : RoomDatabase() {
    abstract fun factDao(): FactDao
    abstract fun noteDao(): NoteDao
    abstract fun conversationDao(): ConversationDao
    abstract fun shortcutDao(): ShortcutDao

    companion object {
        @Volatile
        private var INSTANCE: SimuDatabase? = null

        fun getDatabase(context: Context): SimuDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    SimuDatabase::class.java,
                    "simu_memory.db"
                )
                    .fallbackToDestructiveMigration()
                    .addCallback(object : Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            // Populate default starter data
                            CoroutineScope(Dispatchers.IO).launch {
                                val database = getDatabase(context)
                                val timeFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                                val now = timeFormat.format(Date())

                                // Initial greeting
                                database.conversationDao().insertConversation(
                                    SimuConversation(
                                        role = "assistant",
                                        message = "Good day, Sir. SIMU AI online and standing by. I am ready to execute your voice commands, remember facts, take notes, and launch your applications.",
                                        createdAt = now,
                                        source = "SYSTEM"
                                    )
                                )

                                // Initial starter fact
                                database.factDao().insertFact(
                                    SimuFact(
                                        fact = "User prefers to be addressed respectfully as Sir.",
                                        createdAt = now
                                    )
                                )

                                // Initial starter shortcuts
                                database.shortcutDao().insertAll(
                                    listOf(
                                        SimuShortcut(label = "YouTube", command = "open youtube", packageName = "com.google.android.youtube"),
                                        SimuShortcut(label = "WhatsApp", command = "open whatsapp", packageName = "com.whatsapp"),
                                        SimuShortcut(label = "Chrome", command = "open chrome", packageName = "com.android.chrome"),
                                        SimuShortcut(label = "Camera", command = "open camera", packageName = "com.android.camera"),
                                        SimuShortcut(label = "Settings", command = "open settings", packageName = "com.android.settings"),
                                        SimuShortcut(label = "Search Memory", command = "memory", packageName = "")
                                    )
                                )
                            }
                        }
                    })
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
