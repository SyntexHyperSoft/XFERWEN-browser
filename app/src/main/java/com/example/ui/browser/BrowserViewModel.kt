package com.example.ui.browser

import android.app.Application
import android.webkit.CookieManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.BrowserDatabase
import com.example.data.model.BookmarkEntry
import com.example.data.model.BrowserTab
import com.example.data.model.DnsMode
import com.example.data.model.EncryptedCredential
import com.example.data.model.FasDomainRegistry
import com.example.data.model.HistoryEntry
import com.example.data.model.ProtectionStats
import com.example.data.model.SavedTabEntity
import com.example.data.model.SearchEngine
import com.example.data.preferences.BrowserPreferences
import com.example.data.security.AdBlockEngine
import com.example.data.security.CryptoManager
import com.example.data.security.DohResolver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class BrowserViewModel(application: Application) : AndroidViewModel(application) {

    private val db = BrowserDatabase.getInstance(application)
    private val preferences = BrowserPreferences(application)
    val adBlockEngine = AdBlockEngine(application)
    val dohStatus: StateFlow<DohResolver.DohStatus> = DohResolver.status

    // Settings flows
    val dnsMode: StateFlow<DnsMode> = preferences.dnsMode
    val doNotTrack: StateFlow<Boolean> = preferences.doNotTrack
    val vulkanAcceleration: StateFlow<Boolean> = preferences.vulkanAcceleration
    val webrtcProtection: StateFlow<Boolean> = preferences.webrtcProtection
    val xferwenProtect: StateFlow<Boolean> = preferences.xferwenProtect
    val v8EngineOptimization: StateFlow<Boolean> = preferences.v8EngineOptimization
    val adHiderEnabled: StateFlow<Boolean> = preferences.adHiderEnabled
    val searchEngine: StateFlow<SearchEngine> = preferences.searchEngine
    val isDesktopMode: StateFlow<Boolean> = preferences.desktopMode
    val autoSavePasswords: StateFlow<Boolean> = preferences.autoSavePasswords
    val hasCompletedOnboarding: StateFlow<Boolean> = preferences.hasCompletedOnboarding
    val useSyntexSearchArch: StateFlow<Boolean> = preferences.useSyntexSearchArch

    // Milana AI Real-time protection stats
    val protectionStats: StateFlow<ProtectionStats> = adBlockEngine.stats

    // Tabs management with Room persistence
    private val defaultTab = BrowserTab(url = "https://ya.ru", title = "Яндекс")
    private val _tabs = MutableStateFlow<List<BrowserTab>>(listOf(defaultTab))
    val tabs: StateFlow<List<BrowserTab>> = _tabs.asStateFlow()

    private val _activeTabId = MutableStateFlow(defaultTab.id)
    val activeTabId: StateFlow<String> = _activeTabId.asStateFlow()

    private val _tabsRestored = MutableStateFlow(false)
    private val pendingIncomingUrls = mutableListOf<String>()

    // Current active tab accessor
    val activeTab: BrowserTab?
        get() = _tabs.value.find { it.id == _activeTabId.value } ?: _tabs.value.firstOrNull()

    // Database flows
    val history: StateFlow<List<HistoryEntry>> = db.historyDao().getAllHistory()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val bookmarks: StateFlow<List<BookmarkEntry>> = db.bookmarkDao().getAllBookmarks()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val credentials: StateFlow<List<EncryptedCredential>> = db.credentialDao().getAllCredentials()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    // Pending autofill payload
    private val _autofillPayload = MutableStateFlow<Pair<String, String>?>(null)
    val autofillPayload: StateFlow<Pair<String, String>?> = _autofillPayload.asStateFlow()

    init {
        // Asynchronously restore persistent tabs from Room database without blocking startup
        viewModelScope.launch(Dispatchers.IO) {
            val savedTabs = try { db.tabDao().getAllTabs() } catch (_: Exception) { emptyList() }
            val restoredTabs = if (savedTabs.isNotEmpty()) {
                savedTabs.map {
                    BrowserTab(
                        id = it.id,
                        url = it.url,
                        title = it.title,
                        isIncognito = it.isIncognito
                    )
                }
            } else {
                listOf(defaultTab)
            }

            // Append any incoming URLs received from default browser intent during cold start
            val incomingTabs = synchronized(pendingIncomingUrls) {
                val list = pendingIncomingUrls.map {
                    BrowserTab(url = it, title = "Загрузка...")
                }
                pendingIncomingUrls.clear()
                list
            }

            val finalTabs = (restoredTabs + incomingTabs).distinctBy { it.id }
            _tabs.value = finalTabs
            _activeTabId.value = (incomingTabs.lastOrNull() ?: finalTabs.first()).id
            _tabsRestored.value = true
        }

        // Sync active DNS with DohResolver
        viewModelScope.launch {
            preferences.dnsMode.collect { mode ->
                DohResolver.updateMode(mode)
            }
        }
    }

    /**
     * Handles external link open from Android Intent (ACTION_VIEW) safely without cold start race conditions.
     */
    fun openIncomingUrl(url: String) {
        if (url.isBlank()) return
        if (!_tabsRestored.value) {
            synchronized(pendingIncomingUrls) {
                pendingIncomingUrls.add(url)
            }
        } else {
            addNewTab(url)
        }
    }

    fun persistTabs() {
        val current = _tabs.value
        viewModelScope.launch(Dispatchers.IO) {
            try {
                db.tabDao().clearTabs()
                val entities = current.mapIndexed { index, tab ->
                    SavedTabEntity(
                        id = tab.id,
                        url = if (tab.url.isBlank()) "https://ya.ru" else tab.url,
                        title = if (tab.title.isBlank()) "Вкладка" else tab.title,
                        orderIndex = index,
                        isIncognito = tab.isIncognito
                    )
                }
                db.tabDao().insertAll(entities)
            } catch (_: Exception) {}
        }
    }

    fun setDnsMode(mode: DnsMode) {
        preferences.setDnsMode(mode)
        DohResolver.updateMode(mode)
    }

    fun setDoNotTrack(enabled: Boolean) {
        preferences.setDoNotTrack(enabled)
    }

    fun setVulkanAcceleration(enabled: Boolean) {
        preferences.setVulkanAcceleration(enabled)
    }

    fun setWebRtcProtection(enabled: Boolean) {
        preferences.setWebRtcProtection(enabled)
    }

    fun setXferwenProtect(enabled: Boolean) {
        preferences.setXferwenProtect(enabled)
    }

    fun setV8EngineOptimization(enabled: Boolean) {
        preferences.setV8EngineOptimization(enabled)
    }

    fun setAdHiderEnabled(enabled: Boolean) {
        preferences.setAdHiderEnabled(enabled)
    }

    fun setSearchEngine(engine: SearchEngine) {
        preferences.setSearchEngine(engine)
    }

    fun setAutoSavePasswords(enabled: Boolean) {
        preferences.setAutoSavePasswords(enabled)
    }

    fun setHasCompletedOnboarding(completed: Boolean) {
        preferences.setHasCompletedOnboarding(completed)
    }

    fun setUseSyntexSearchArch(enabled: Boolean) {
        preferences.setUseSyntexSearchArch(enabled)
    }

    fun toggleDesktopMode() {
        val next = !isDesktopMode.value
        preferences.setDesktopMode(next)
        activeTab?.let { tab ->
            val wv = TabWebViewManager.get(tab.id)
            wv?.settings?.userAgentString = if (next) {
                com.example.data.security.StealthAntiBot.REAL_DESKTOP_CHROME_UA
            } else {
                com.example.data.security.StealthAntiBot.REAL_MOBILE_CHROME_UA
            }
            wv?.reload()
        }
    }

    fun navigateTo(input: String) {
        val trimmed = input.trim()
        if (trimmed.isBlank()) return

        // 1. Check FAS Ecosystem Domain & Alias Registry first!
        val fasTarget = FasDomainRegistry.resolveInput(trimmed)
        val (targetUrl, customDisplay) = if (fasTarget != null) {
            val fasDisplay = FasDomainRegistry.toFasDisplayUrl(fasTarget)
                ?: if (trimmed.startsWith("fas://", ignoreCase = true)) trimmed else "fas://$trimmed"
            Pair(fasTarget, fasDisplay)
        } else {
            val lower = trimmed.lowercase().trimEnd('/')
            when {
                lower in setOf("fas://xferven.browse.com/home", "fas://home", "xferwen://home", "home") ->
                    Pair("about:blank", null)
                trimmed.startsWith("http://") || trimmed.startsWith("https://") ->
                    Pair(trimmed, FasDomainRegistry.toFasDisplayUrl(trimmed))
                trimmed.contains(".") && !trimmed.contains(" ") ->
                    Pair("https://$trimmed", FasDomainRegistry.toFasDisplayUrl("https://$trimmed"))
                searchEngine.value == SearchEngine.XFERWEN_FAS -> {
                    val encoded = java.net.URLEncoder.encode(trimmed, "UTF-8")
                    Pair("https://xferwen.search/?q=$encoded", "fas://search?q=$encoded")
                }
                else -> {
                    val encoded = java.net.URLEncoder.encode(trimmed, "UTF-8")
                    val resolved = "${searchEngine.value.searchUrl}$encoded"
                    Pair(resolved, FasDomainRegistry.toFasDisplayUrl(resolved))
                }
            }
        }

        activeTab?.let { tab ->
            val updated = tab.copy(
                url = targetUrl,
                customDisplayUrl = customDisplay,
                title = if (targetUrl == "about:blank") "Новая вкладка" else tab.title,
                isLoading = targetUrl != "about:blank"
            )
            _tabs.update { list -> list.map { if (it.id == tab.id) updated else it } }
            val wv = TabWebViewManager.get(tab.id)
            if (targetUrl == "about:blank") {
                wv?.loadUrl("about:blank")
            } else {
                wv?.loadUrl(targetUrl)
            }
            persistTabs()
        }
    }

    fun goHome() {
        activeTab?.let { tab ->
            val updated = tab.copy(
                url = "about:blank",
                customDisplayUrl = null,
                title = "Новая вкладка",
                isLoading = false
            )
            _tabs.update { list -> list.map { if (it.id == tab.id) updated else it } }
            TabWebViewManager.get(tab.id)?.loadUrl("about:blank")
            persistTabs()
        }
    }

    fun reloadCurrentTab() {
        activeTab?.let { tab ->
            TabWebViewManager.get(tab.id)?.reload()
        }
    }

    fun goBack(): Boolean {
        activeTab?.let { tab ->
            val wv = TabWebViewManager.get(tab.id)
            if (wv?.canGoBack() == true) {
                wv.goBack()
                return true
            }
        }
        return false
    }

    fun goForward(): Boolean {
        activeTab?.let { tab ->
            val wv = TabWebViewManager.get(tab.id)
            if (wv?.canGoForward() == true) {
                wv.goForward()
                return true
            }
        }
        return false
    }

    /**
     * Adds a new tab and immediately switches to it, keeping all existing tabs intact.
     */
    fun addNewTab(url: String = "https://ya.ru") {
        val newTab = BrowserTab(
            url = url,
            title = if (url == "https://ya.ru") "Яндекс" else if (url == "about:blank") "Новая вкладка" else "Загрузка..."
        )
        _tabs.update { it + newTab }
        _activeTabId.value = newTab.id
        persistTabs()
    }

    fun selectTab(id: String) {
        if (_tabs.value.any { it.id == id }) {
            _activeTabId.value = id
        }
    }

    fun closeTab(id: String) {
        val currentTabs = _tabs.value
        TabWebViewManager.remove(id)

        if (currentTabs.size <= 1) {
            val fresh = BrowserTab(url = "https://ya.ru", title = "Яндекс")
            _tabs.value = listOf(fresh)
            _activeTabId.value = fresh.id
            persistTabs()
            return
        }

        val updated = currentTabs.filterNot { it.id == id }
        _tabs.value = updated
        if (_activeTabId.value == id) {
            _activeTabId.value = updated.last().id
        }
        persistTabs()
    }

    fun closeAllTabs() {
        TabWebViewManager.clearAll()
        val fresh = BrowserTab(url = "https://ya.ru", title = "Яндекс")
        _tabs.value = listOf(fresh)
        _activeTabId.value = fresh.id
        persistTabs()
    }

    /**
     * Updates URL, title, progress, and navigation state.
     * Emits a fresh BrowserTab copy to ensure Compose address bar updates instantaneously.
     */
    fun updateTabInfo(id: String, title: String?, url: String?, progress: Int?, isLoading: Boolean?) {
        val current = _tabs.value
        val tab = current.find { it.id == id } ?: return
        val wv = TabWebViewManager.get(id)

        val newUrl = if (!url.isNullOrBlank()) url else tab.url
        val newTitle = if (!title.isNullOrBlank()) title else tab.title
        val newProgress = progress ?: tab.progress
        val newLoading = isLoading ?: tab.isLoading
        val canBack = wv?.canGoBack() ?: tab.canGoBack
        val canFwd = wv?.canGoForward() ?: tab.canGoForward

        val fasDisplay = FasDomainRegistry.toFasDisplayUrl(newUrl)
        val newCustomDisplay = fasDisplay ?: if (newUrl.startsWith("http")) null else tab.customDisplayUrl

        if (tab.url != newUrl || tab.title != newTitle || tab.progress != newProgress ||
            tab.isLoading != newLoading || tab.canGoBack != canBack || tab.canGoForward != canFwd ||
            tab.customDisplayUrl != newCustomDisplay) {
            val updated = tab.copy(
                url = newUrl,
                customDisplayUrl = newCustomDisplay,
                title = newTitle,
                progress = newProgress,
                isLoading = newLoading,
                canGoBack = canBack,
                canGoForward = canFwd
            )
            _tabs.update { list -> list.map { if (it.id == id) updated else it } }
            if (tab.url != newUrl || tab.title != newTitle) {
                persistTabs()
            }
        }
    }

    fun recordHistory(url: String, title: String) {
        if (url.isBlank() || url == "about:blank" || url.startsWith("data:")) return
        viewModelScope.launch(Dispatchers.IO) {
            db.historyDao().insertHistory(
                HistoryEntry(url = url, title = title.ifBlank { url })
            )
        }
    }

    fun toggleBookmark(url: String, title: String) {
        viewModelScope.launch(Dispatchers.IO) {
            db.bookmarkDao().insertBookmark(
                BookmarkEntry(url = url, title = title.ifBlank { url })
            )
        }
    }

    fun deleteBookmark(entry: BookmarkEntry) {
        viewModelScope.launch(Dispatchers.IO) {
            db.bookmarkDao().deleteBookmark(entry)
        }
    }

    fun deleteHistoryEntry(id: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            db.historyDao().deleteHistoryById(id)
        }
    }

    fun clearAllHistory() {
        viewModelScope.launch(Dispatchers.IO) {
            db.historyDao().clearHistory()
        }
    }

    fun saveCredential(domain: String, username: String, plainPass: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val encResult = CryptoManager.encrypt(plainPass)
            val entity = EncryptedCredential(
                domain = domain,
                username = username,
                encryptedPasswordBase64 = encResult.ciphertextBase64,
                ivBase64 = encResult.ivBase64
            )
            db.credentialDao().insertCredential(entity)
        }
    }

    fun deleteCredential(id: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            db.credentialDao().deleteCredentialById(id)
        }
    }

    fun requestAutofill(username: String, decryptedPass: String) {
        _autofillPayload.value = Pair(username, decryptedPass)
    }

    fun clearAutofillPayload() {
        _autofillPayload.value = null
    }

    fun clearCacheAndCookies() {
        viewModelScope.launch(Dispatchers.Main) {
            CookieManager.getInstance().removeAllCookies(null)
            CookieManager.getInstance().flush()
            _tabs.value.forEach { tab ->
                val wv = TabWebViewManager.get(tab.id)
                wv?.clearCache(true)
                wv?.clearFormData()
                wv?.clearHistory()
            }
            adBlockEngine.resetStats()
        }
    }
}
