package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Modes of DNS and privacy protection in XFERWEN.
 */
enum class DnsMode(
    val title: String,
    val description: String,
    val dohUrl: String
) {
    SYNTEXT_BYPASS(
        title = "Обход блокировок + Милана V3.2 Flash",
        description = "Интеллектуальный обход блокировок ChatGPT, Gemini, Claude и нейрофильтрация рекламы с нейросетью Milana V3.2 Flash.",
        dohUrl = "https://eu.geohide.ru/dns-query"
    ),
    MILANA_AI_MAX_SHIELD(
        title = "Максимальная защита с Милана V3.2 Ultra",
        description = "Максимальная нейросетевая блокировка рекламы, трекеров и фишинга от старшей сестры Milana V3.2 Ultra.",
        dohUrl = "https://dns.adguard-dns.com/dns-query"
    );

    val displayName: String
        get() = title
}

/**
 * Real-time statistics for Milana AI Cyber Shield.
 */
data class ProtectionStats(
    val blockedAds: Int = 0,
    val blockedTrackers: Int = 0,
    val blockedPhishing: Int = 0
) {
    val totalBlocked: Int
        get() = blockedAds + blockedTrackers + blockedPhishing
}

/**
 * Browsing history entry stored locally in Room.
 */
@Entity(tableName = "history")
data class HistoryEntry(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val url: String,
    val title: String,
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * User bookmarks stored locally in Room.
 */
@Entity(tableName = "bookmarks")
data class BookmarkEntry(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val url: String,
    val title: String,
    val createdAt: Long = System.currentTimeMillis()
)

/**
 * Encrypted password vault entity.
 * Passwords are stored encrypted with AES-256-GCM.
 */
@Entity(tableName = "encrypted_credentials")
data class EncryptedCredential(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val domain: String,
    val username: String,
    val encryptedPasswordBase64: String,
    val ivBase64: String,
    val createdAt: Long = System.currentTimeMillis()
)

/**
 * Search engine option.
 */
enum class SearchEngine(
    val title: String,
    val description: String,
    val searchUrl: String,
    val homeUrl: String,
    val isExclusiveToXferwen: Boolean = false
) {
    XFERWEN_FAS(
        title = "XFERWEN + FAS (Фирменный кибер-поиск)",
        description = "Эксклюзивная поисковая система экосистемы SyntexHyperSoft и каталога FAS",
        searchUrl = "https://xferwen.search/?q=",
        homeUrl = "about:blank",
        isExclusiveToXferwen = true
    ),
    YANDEX(
        title = "Яндекс",
        description = "Классический веб-поиск Яндекс с нейрофильтрацией Milana AI",
        searchUrl = "https://yandex.ru/search/?text=",
        homeUrl = "https://ya.ru"
    ),
    GOOGLE(
        title = "Google",
        description = "Глобальный поиск Google",
        searchUrl = "https://www.google.com/search?q=",
        homeUrl = "https://www.google.com"
    ),
    DUCKDUCKGO(
        title = "DuckDuckGo",
        description = "Конфиденциальный поиск DuckDuckGo",
        searchUrl = "https://duckduckgo.com/?q=",
        homeUrl = "https://duckduckgo.com"
    )
}

/**
 * Persistent tab entity to save and restore open tabs across sessions.
 */
@Entity(tableName = "saved_tabs")
data class SavedTabEntity(
    @PrimaryKey val id: String,
    val url: String,
    val title: String,
    val orderIndex: Int = 0,
    val isIncognito: Boolean = false
)

