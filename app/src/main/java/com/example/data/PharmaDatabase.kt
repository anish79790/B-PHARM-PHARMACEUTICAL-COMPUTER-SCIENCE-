package com.example.data

import android.content.Context
import androidx.room.*
import kotlinx.coroutines.flow.Flow

// --- entities ---

@Entity(tableName = "users")
data class User(
    @PrimaryKey val email: String,
    val username: String,
    val fullName: String,
    val passwordHash: String,
    val age: Int,
    val gender: String,
    val healthGoal: String
)

@Entity(tableName = "medical_reports")
data class MedicalReport(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userEmail: String = "",
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
    val userEmail: String = "",
    val role: String, // "user" or "model"
    val content: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "bookmarked_items", primaryKeys = ["id", "userEmail"])
data class BookmarkedItem(
    val id: String, // e.g. "disease_diabetes", "drug_metformin"
    val userEmail: String = "",
    val type: String, // "disease" or "drug"
    val name: String,
    val subtitle: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "medicine_expenses")
data class MedicineExpense(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userEmail: String = "",
    val drugName: String,
    val cost: Double,
    val date: Long = System.currentTimeMillis(),
    val category: String
)

@Entity(tableName = "user_diseases")
data class UserDisease(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userEmail: String = "",
    val diseaseId: String,
    val name: String,
    val dateAdded: Long = System.currentTimeMillis()
)

// --- DAOs ---

@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun getUserByEmail(email: String): User?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: User)
}

@Dao
interface MedicalReportDao {
    @Query("SELECT * FROM medical_reports WHERE userEmail = :userEmail ORDER BY date DESC")
    fun getAllReportsForUser(userEmail: String): Flow<List<MedicalReport>>

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
    @Query("SELECT * FROM chat_messages WHERE userEmail = :userEmail ORDER BY timestamp ASC")
    fun getChatHistoryForUser(userEmail: String): Flow<List<ChatMessageEntity>>

    @Query("SELECT * FROM chat_messages ORDER BY timestamp ASC")
    fun getChatHistory(): Flow<List<ChatMessageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: ChatMessageEntity)

    @Query("DELETE FROM chat_messages WHERE userEmail = :userEmail")
    suspend fun clearHistoryForUser(userEmail: String)

    @Query("DELETE FROM chat_messages")
    suspend fun clearHistory()
}

@Dao
interface BookmarkDao {
    @Query("SELECT * FROM bookmarked_items WHERE userEmail = :userEmail ORDER BY timestamp DESC")
    fun getAllBookmarksForUser(userEmail: String): Flow<List<BookmarkedItem>>

    @Query("SELECT * FROM bookmarked_items ORDER BY timestamp DESC")
    fun getAllBookmarks(): Flow<List<BookmarkedItem>>

    @Query("SELECT EXISTS(SELECT 1 FROM bookmarked_items WHERE id = :id AND userEmail = :userEmail)")
    fun isBookmarkedFlow(id: String, userEmail: String): Flow<Boolean>

    @Query("SELECT EXISTS(SELECT 1 FROM bookmarked_items WHERE id = :id)")
    fun isBookmarkedFlow(id: String): Flow<Boolean>

    @Query("SELECT EXISTS(SELECT 1 FROM bookmarked_items WHERE id = :id AND userEmail = :userEmail)")
    suspend fun isBookmarked(id: String, userEmail: String): Boolean

    @Query("SELECT EXISTS(SELECT 1 FROM bookmarked_items WHERE id = :id)")
    suspend fun isBookmarked(id: String): Boolean

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBookmark(bookmark: BookmarkedItem)

    @Delete
    suspend fun deleteBookmark(bookmark: BookmarkedItem)
}

@Dao
interface MedicineExpenseDao {
    @Query("SELECT * FROM medicine_expenses WHERE userEmail = :userEmail ORDER BY date DESC")
    fun getAllExpensesForUser(userEmail: String): Flow<List<MedicineExpense>>

    @Query("SELECT * FROM medicine_expenses ORDER BY date DESC")
    fun getAllExpenses(): Flow<List<MedicineExpense>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpense(expense: MedicineExpense)

    @Query("DELETE FROM medicine_expenses WHERE id = :id")
    suspend fun deleteExpenseById(id: Int)

    @Query("DELETE FROM medicine_expenses WHERE userEmail = :userEmail")
    suspend fun clearAllForUser(userEmail: String)

    @Query("DELETE FROM medicine_expenses")
    suspend fun clearAll()
}

@Dao
interface UserDiseaseDao {
    @Query("SELECT * FROM user_diseases WHERE userEmail = :userEmail ORDER BY dateAdded DESC")
    fun getAllUserDiseasesForUser(userEmail: String): Flow<List<UserDisease>>

    @Query("SELECT * FROM user_diseases ORDER BY dateAdded DESC")
    fun getAllUserDiseases(): Flow<List<UserDisease>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserDisease(userDisease: UserDisease)

    @Query("DELETE FROM user_diseases WHERE diseaseId = :diseaseId AND userEmail = :userEmail")
    suspend fun deleteUserDisease(diseaseId: String, userEmail: String)

    @Query("DELETE FROM user_diseases WHERE diseaseId = :diseaseId")
    suspend fun deleteUserDisease(diseaseId: String)

    @Query("SELECT EXISTS(SELECT 1 FROM user_diseases WHERE diseaseId = :diseaseId AND userEmail = :userEmail)")
    suspend fun hasDisease(diseaseId: String, userEmail: String): Boolean

    @Query("SELECT EXISTS(SELECT 1 FROM user_diseases WHERE diseaseId = :diseaseId)")
    suspend fun hasDisease(diseaseId: String): Boolean

    @Query("DELETE FROM user_diseases WHERE userEmail = :userEmail")
    suspend fun clearAllForUser(userEmail: String)

    @Query("DELETE FROM user_diseases")
    suspend fun clearAll()
}

// --- AppDatabase ---

@Database(
    entities = [
        User::class,
        MedicalReport::class,
        ChatMessageEntity::class,
        BookmarkedItem::class,
        MedicineExpense::class,
        UserDisease::class
    ],
    version = 3,
    exportSchema = false
)
abstract class PharmaDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun medicalReportDao(): MedicalReportDao
    abstract fun chatMessageDao(): ChatMessageDao
    abstract fun bookmarkDao(): BookmarkDao
    abstract fun medicineExpenseDao(): MedicineExpenseDao
    abstract fun userDiseaseDao(): UserDiseaseDao

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
