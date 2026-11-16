package com.example.data.db

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.model.BookmarkEntry
import com.example.data.model.EncryptedCredential
import com.example.data.model.HistoryEntry
import com.example.data.model.SavedTabEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface HistoryDao {
    @Query("SELECT * FROM history ORDER BY timestamp DESC")
    fun getAllHistory(): Flow<List<HistoryEntry>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistory(entry: HistoryEntry): Long

    @Query("DELETE FROM history WHERE id = :id")
    suspend fun deleteHistoryById(id: Long)

    @Query("DELETE FROM history")
    suspend fun clearHistory()
}

@Dao
interface BookmarkDao {
    @Query("SELECT * FROM bookmarks ORDER BY createdAt DESC")
    fun getAllBookmarks(): Flow<List<BookmarkEntry>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBookmark(entry: BookmarkEntry): Long

    @Delete
    suspend fun deleteBookmark(entry: BookmarkEntry)

    @Query("SELECT EXISTS(SELECT 1 FROM bookmarks WHERE url = :url LIMIT 1)")
    fun isBookmarked(url: String): Flow<Boolean>
}

@Dao
interface CredentialDao {
    @Query("SELECT * FROM encrypted_credentials ORDER BY createdAt DESC")
    fun getAllCredentials(): Flow<List<EncryptedCredential>>

    @Query("SELECT * FROM encrypted_credentials WHERE domain LIKE '%' || :domain || '%'")
    suspend fun findCredentialsForDomain(domain: String): List<EncryptedCredential>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCredential(credential: EncryptedCredential): Long

    @Delete
    suspend fun deleteCredential(credential: EncryptedCredential)

    @Query("DELETE FROM encrypted_credentials WHERE id = :id")
    suspend fun deleteCredentialById(id: Long)
}

@Dao
interface TabDao {
    @Query("SELECT * FROM saved_tabs ORDER BY orderIndex ASC")
    suspend fun getAllTabs(): List<SavedTabEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(tabs: List<SavedTabEntity>)

    @Query("DELETE FROM saved_tabs")
    suspend fun clearTabs()
}

@Database(
    entities = [HistoryEntry::class, BookmarkEntry::class, EncryptedCredential::class, SavedTabEntity::class],
    version = 2,
    exportSchema = false
)
abstract class BrowserDatabase : RoomDatabase() {
    abstract fun historyDao(): HistoryDao
    abstract fun bookmarkDao(): BookmarkDao
    abstract fun credentialDao(): CredentialDao
    abstract fun tabDao(): TabDao

    companion object {
        @Volatile
        private var INSTANCE: BrowserDatabase? = null

        fun getInstance(context: Context): BrowserDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    BrowserDatabase::class.java,
                    "xferwen_browser_vault.db"
                ).fallbackToDestructiveMigration(true).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
