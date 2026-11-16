package com.example.data.model

import android.graphics.Bitmap
import java.util.UUID

/**
 * Immutable model representing an open browser tab.
 */
data class BrowserTab(
    val id: String = UUID.randomUUID().toString(),
    val url: String = "https://ya.ru",
    val title: String = "Новая вкладка",
    val progress: Int = 0,
    val isLoading: Boolean = false,
    val canGoBack: Boolean = false,
    val canGoForward: Boolean = false,
    val favicon: Bitmap? = null,
    val isIncognito: Boolean = false,
    val customDisplayUrl: String? = null
) {
    val displayUrl: String
        get() = customDisplayUrl ?: FasDomainRegistry.toFasDisplayUrl(url) ?: url
}
