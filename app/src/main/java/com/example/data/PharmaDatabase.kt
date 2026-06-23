package com.example.data

import android.content.Context
import androidx.room.*
import kotlinx.coroutines.flow.Flow

// --- entities ---

@Entity(tableName = "medical_reports")
data class MedicalReport(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val date: Long = System.currentTimeMillis(),
    val labData: String, // JSON or string content
    val analysisMarkdown: String, // Gemini result
    val possibleIndication: String,
    val riskLevel: String // "Low", "Moderate", "High"
)

@Entity(tableName = "chat_messages")
data class ChatMessageEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val role: String, // "user" or "model"
    val content: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "bookmarked_items")
data class BookmarkedItem(
    @PrimaryKey val id: String, // e.g. "disease_diabetes", "drug_metformin"
    val type: String, // "disease" or "drug"
    val name: String,
    val subtitle: String,
    val timestamp: Long = System.currentTimeMillis()
)

// --- DAOs ---

@Dao
interface MedicalReportDao {
    @Query("SELECT * FROM medical_reports ORDER BY date DESC")
    fun getAllReports(): Flow<List<MedicalReport>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReport(report: MedicalReport)

    @Query("DELETE FROM medical_reports WHERE id = :id")
    suspend fun deleteReportById(id: Int)

    @Query("DELETE FROM medical_reports")
    suspend fun clearAll()
}

@Dao
interface ChatMessageDao {
    @Query("SELECT * FROM chat_messages ORDER BY timestamp ASC")
    fun getChatHistory(): Flow<List<ChatMessageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: ChatMessageEntity)

    @Query("DELETE FROM chat_messages")
    suspend fun clearHistory()
}

@Dao
interface BookmarkDao {
    @Query("SELECT * FROM bookmarked_items ORDER BY timestamp DESC")
    fun getAllBookmarks(): Flow<List<BookmarkedItem>>

    @Query("SELECT EXISTS(SELECT 1 FROM bookmarked_items WHERE id = :id)")
    fun isBookmarkedFlow(id: String): Flow<Boolean>

    @Query("SELECT EXISTS(SELECT 1 FROM bookmarked_items WHERE id = :id)")
    suspend fun isBookmarked(id: String): Boolean

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBookmark(bookmark: BookmarkedItem)

    @Delete
    suspend fun deleteBookmark(bookmark: BookmarkedItem)
}

// --- AppDatabase ---

@Database(entities = [MedicalReport::class, ChatMessageEntity::class, BookmarkedItem::class], version = 1, exportSchema = false)
abstract class PharmaDatabase : RoomDatabase() {
    abstract fun medicalReportDao(): MedicalReportDao
    abstract fun chatMessageDao(): ChatMessageDao
    abstract fun bookmarkDao(): BookmarkDao

    companion object {
        @Volatile
        private var INSTANCE: PharmaDatabase? = null

        fun getDatabase(context: Context): PharmaDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    PharmaDatabase::class.java,
                    "pharmasense_database"
                )
                .fallbackToDestructiveMigration(true)
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
