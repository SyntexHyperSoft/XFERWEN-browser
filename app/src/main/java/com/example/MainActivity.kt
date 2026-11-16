package com.example

import android.app.role.RoleManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.webkit.ProxyController
import androidx.webkit.WebViewFeature
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.FileProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.DnsMode
import com.example.ui.browser.BrowserViewModel
import com.example.ui.browser.BrowserWebView
import com.example.ui.components.BookmarksSheet
import com.example.ui.components.CyberAddressBar
import com.example.ui.components.CyberBottomNav
import com.example.ui.components.HistorySheet
import com.example.ui.components.MilanaAiBottomSheet
import com.example.ui.components.NewTabDashboard
import com.example.ui.components.OnboardingWizardDialog
import com.example.ui.components.PasswordVaultDialog
import com.example.ui.components.ProtectionSettingsDialog
import com.example.ui.components.SavePasswordOfferDialog
import com.example.ui.components.TabsManagerSheet
import com.example.ui.theme.CyberVoid
import com.example.ui.theme.MyApplicationTheme
import kotlinx.coroutines.launch
import java.io.File

class MainActivity : ComponentActivity() {

    private val viewModel: BrowserViewModel by viewModels()

    // Activity launcher for Android 10+ default browser role dialog
    private val defaultBrowserRoleLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            Toast.makeText(this, "XFERWEN успешно установлен браузером по умолчанию!", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Enable hardware accelerated window for high-performance Vulkan / GPU rasterization
        window.setFlags(
            android.view.WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,
            android.view.WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED
        )

        // Ensure native direct BoringSSL connection
        if (WebViewFeature.isFeatureSupported(WebViewFeature.PROXY_OVERRIDE)) {
            try {
                ProxyController.getInstance().clearProxyOverride(
                    { it.run() },
                    { }
                )
            } catch (_: Throwable) {}
        }

        // Handle incoming view intent if opened as default browser
        handleIncomingIntent(intent)

        setContent {
            MyApplicationTheme {
                MainBrowserScreen(
                    viewModel = viewModel,
                    onMakeDefaultBrowser = { requestDefaultBrowserRole() },
                    onShareApk = { shareApk() }
                )
            }
        }
    }

    override fun onPause() {
        super.onPause()
        viewModel.persistTabs()
    }

    override fun onStop() {
        super.onStop()
        viewModel.persistTabs()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIncomingIntent(intent)
    }

    private fun handleIncomingIntent(intent: Intent?) {
        val uri = intent?.data ?: return
        val scheme = uri.scheme?.lowercase()
        if (scheme == "http" || scheme == "https") {
            viewModel.openIncomingUrl(uri.toString())
        }
    }

    /**
     * Complete and robust Default Browser setup for Android 9 (Pie, API 28) and Android 10+ (API 29+).
     */
    private fun requestDefaultBrowserRole() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val roleManager = getSystemService(Context.ROLE_SERVICE) as? RoleManager
            if (roleManager != null && roleManager.isRoleAvailable(RoleManager.ROLE_BROWSER)) {
                if (!roleManager.isRoleHeld(RoleManager.ROLE_BROWSER)) {
                    val intent = roleManager.createRequestRoleIntent(RoleManager.ROLE_BROWSER)
                    defaultBrowserRoleLauncher.launch(intent)
                    return
                } else {
                    Toast.makeText(this, "XFERWEN уже установлен как браузер по умолчанию", Toast.LENGTH_SHORT).show()
                    return
                }
            }
        }

        // Support for Android 9 (API 28) and devices without RoleManager
        var handled = false
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            try {
                val intent = Intent(Settings.ACTION_MANAGE_DEFAULT_APPS_SETTINGS)
                startActivity(intent)
                handled = true
            } catch (_: Exception) {}
        }

        if (!handled) {
            try {
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.parse("package:$packageName")
                }
                startActivity(intent)
                Toast.makeText(this, "Выберите XFERWEN в качестве браузера по умолчанию", Toast.LENGTH_LONG).show()
                handled = true
            } catch (_: Exception) {}
        }

        if (!handled) {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://ya.ru")).apply {
                addCategory(Intent.CATEGORY_BROWSABLE)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            startActivity(Intent.createChooser(intent, "Выберите XFERWEN и нажмите 'Всегда'"))
        }
    }

    private fun shareApk() {
        try {
            val appInfo = applicationInfo
            val sourceApk = File(appInfo.sourceDir)
            val cacheApk = File(cacheDir, "XFERWEN_v1.0.0.0.apk")
            sourceApk.copyTo(cacheApk, overwrite = true)

            val apkUri = FileProvider.getUriForFile(
                this,
                "${packageName}.provider",
                cacheApk
            )

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/vnd.android.package-archive"
                putExtra(Intent.EXTRA_STREAM, apkUri)
                putExtra(Intent.EXTRA_SUBJECT, "Браузер XFERWEN v1.0.0.0 APK")
                putExtra(
                    Intent.EXTRA_TEXT,
                    "Установите XFERWEN v1.0.0.0 — быстрый кибер-браузер с защитой Milana AI, обходом блокировок и шифрованием паролей!"
                )
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            startActivity(Intent.createChooser(shareIntent, "Поделиться APK файлом XFERWEN"))
        } catch (e: Exception) {
            Toast.makeText(this, "Не удалось передать APK: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainBrowserScreen(
    viewModel: BrowserViewModel,
    onMakeDefaultBrowser: () -> Unit,
    onShareApk: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    // State from ViewModel
    val tabs by viewModel.tabs.collectAsStateWithLifecycle()
    val activeTabId by viewModel.activeTabId.collectAsStateWithLifecycle()
    val activeTab = remember(tabs, activeTabId) {
        tabs.find { it.id == activeTabId } ?: tabs.firstOrNull()
    }

    val dnsMode by viewModel.dnsMode.collectAsStateWithLifecycle()
    val doNotTrack by viewModel.doNotTrack.collectAsStateWithLifecycle()
    val vulkanAcceleration by viewModel.vulkanAcceleration.collectAsStateWithLifecycle()
    val webrtcProtection by viewModel.webrtcProtection.collectAsStateWithLifecycle()
    val xferwenProtect by viewModel.xferwenProtect.collectAsStateWithLifecycle()
    val v8EngineOptimization by viewModel.v8EngineOptimization.collectAsStateWithLifecycle()
    val adHiderEnabled by viewModel.adHiderEnabled.collectAsStateWithLifecycle()
    val searchEngine by viewModel.searchEngine.collectAsStateWithLifecycle()
    val isDesktopMode by viewModel.isDesktopMode.collectAsStateWithLifecycle()
    val protectionStats by viewModel.protectionStats.collectAsStateWithLifecycle()

    val historyList by viewModel.history.collectAsStateWithLifecycle()
    val bookmarksList by viewModel.bookmarks.collectAsStateWithLifecycle()
    val credentialsList by viewModel.credentials.collectAsStateWithLifecycle()
    val autofillPayload by viewModel.autofillPayload.collectAsStateWithLifecycle()

    val autoSavePasswords by viewModel.autoSavePasswords.collectAsStateWithLifecycle()
    val hasCompletedOnboarding by viewModel.hasCompletedOnboarding.collectAsStateWithLifecycle()
    val useSyntexSearchArch by viewModel.useSyntexSearchArch.collectAsStateWithLifecycle()

    // Captured password offer state
    var pendingPasswordSave by remember { mutableStateOf<Triple<String, String, String>?>(null) }

    // Fullscreen Video State
    var customVideoView by remember { mutableStateOf<View?>(null) }
    var customVideoCallback by remember { mutableStateOf<WebChromeClient.CustomViewCallback?>(null) }

    // Sheet visibility states
    var showShieldSheet by remember { mutableStateOf(false) }
    var showTabsSheet by remember { mutableStateOf(false) }
    var showHistorySheet by remember { mutableStateOf(false) }
    var showBookmarksSheet by remember { mutableStateOf(false) }
    var showPasswordsSheet by remember { mutableStateOf(false) }
    var showSettingsSheet by remember { mutableStateOf(false) }

    val shieldSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val tabsSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val historySheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val bookmarksSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val passwordsSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val settingsSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // Handle Android System Back: video fullscreen exit first, then webview back, then tab close
    BackHandler {
        if (customVideoView != null) {
            customVideoCallback?.onCustomViewHidden()
            customVideoView = null
            customVideoCallback = null
        } else if (!viewModel.goBack()) {
            if (tabs.size > 1) {
                viewModel.closeTab(activeTabId)
            }
        }
    }

    val currentUrl = activeTab?.displayUrl.orEmpty()
    val realUrl = activeTab?.url.orEmpty()
    val currentTitle = activeTab?.title.orEmpty()
    val isLoading = activeTab?.isLoading ?: false
    val progress = activeTab?.progress ?: 0
    val canGoBack = activeTab?.canGoBack ?: false
    val canGoForward = activeTab?.canGoForward ?: false

    val isBlankNewTab = realUrl.isBlank() || realUrl == "about:blank"

    // If video is fullscreen, show edge-to-edge custom view
    if (customVideoView != null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
        ) {
            AndroidView(
                factory = {
                    customVideoView!!.apply {
                        layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )
                    }
                },
                modifier = Modifier.fillMaxSize()
            )

            // Close fullscreen video floating button
            IconButton(
                onClick = {
                    customVideoCallback?.onCustomViewHidden()
                    customVideoView = null
                    customVideoCallback = null
                },
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.6f))
                    .size(44.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Exit Fullscreen",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    } else {
        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = {
                CyberAddressBar(
                    currentUrl = currentUrl,
                    pageTitle = currentTitle,
                    isLoading = isLoading,
                    progress = progress,
                    totalBlocked = protectionStats.totalBlocked,
                    tabCount = tabs.size,
                    isDesktopMode = isDesktopMode,
                    onNavigate = { viewModel.navigateTo(it) },
                    onReload = { viewModel.reloadCurrentTab() },
                    onOpenShield = { showShieldSheet = true },
                    onOpenTabs = { showTabsSheet = true },
                    onOpenHistory = { showHistorySheet = true },
                    onOpenBookmarks = { showBookmarksSheet = true },
                    onOpenPasswords = { showPasswordsSheet = true },
                    onOpenSettings = { showSettingsSheet = true },
                    onGoHome = { viewModel.goHome() },
                    onToggleDesktop = { viewModel.toggleDesktopMode() },
                    onMakeDefaultBrowser = onMakeDefaultBrowser,
                    modifier = Modifier.windowInsetsPadding(WindowInsets.statusBars)
                )
            },
            bottomBar = {
                CyberBottomNav(
                    canGoBack = canGoBack,
                    canGoForward = canGoForward,
                    tabCount = tabs.size,
                    totalBlocked = protectionStats.totalBlocked,
                    onBack = { viewModel.goBack() },
                    onForward = { viewModel.goForward() },
                    onOpenShield = { showShieldSheet = true },
                    onBookmark = {
                        if (currentUrl.isNotBlank() && currentUrl != "about:blank") {
                            viewModel.toggleBookmark(currentUrl, currentTitle)
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar("Закладка сохранена в XFERWEN")
                            }
                        }
                    },
                    onOpenTabs = { showTabsSheet = true },
                    onNewTab = { viewModel.addNewTab("https://ya.ru") }
                )
            },
            containerColor = CyberVoid
        ) { innerPadding ->
            // Use innerPadding properly so pages and address bar never overlap or clip
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .background(CyberVoid)
            ) {
                if (isBlankNewTab) {
                    NewTabDashboard(
                        dnsMode = dnsMode,
                        totalBlocked = protectionStats.totalBlocked,
                        isSyntexSearchArch = useSyntexSearchArch,
                        onNavigate = { viewModel.navigateTo(it) },
                        onOpenShield = { showShieldSheet = true },
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    activeTab?.let { tab ->
                        BrowserWebView(
                            tab = tab,
                            dnsMode = dnsMode,
                            isDntEnabled = doNotTrack,
                            isVulkanEnabled = vulkanAcceleration,
                            isWebRtcProtectionEnabled = webrtcProtection,
                            isXferwenProtectEnabled = xferwenProtect,
                            isV8OptimizationEnabled = v8EngineOptimization,
                            isAdHiderEnabled = adHiderEnabled,
                            adBlockEngine = viewModel.adBlockEngine,
                            autofillPayload = autofillPayload,
                            onUpdateTabInfo = { title, url, prog, loading ->
                                viewModel.updateTabInfo(tab.id, title, url, prog, loading)
                            },
                            onRecordHistory = { url, title ->
                                viewModel.recordHistory(url, title)
                            },
                            onAutofillConsumed = {
                                viewModel.clearAutofillPayload()
                            },
                            onOfferSavePassword = { dom, user, pass ->
                                if (autoSavePasswords) {
                                    pendingPasswordSave = Triple(dom, user, pass)
                                }
                            },
                            onShowCustomView = { view, callback ->
                                customVideoView = view
                                customVideoCallback = callback
                            },
                            onHideCustomView = {
                                customVideoView = null
                                customVideoCallback?.onCustomViewHidden()
                                customVideoCallback = null
                            },
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }
        }
    }

    // Milana AI Cyber Shield Bottom Sheet
    if (showShieldSheet) {
        MilanaAiBottomSheet(
            sheetState = shieldSheetState,
            stats = protectionStats,
            currentMode = dnsMode,
            onModeChange = { newMode ->
                viewModel.setDnsMode(newMode)
                coroutineScope.launch {
                    val modeName = if (newMode == DnsMode.SYNTEXT_BYPASS) "Обход блокировок" else "Максимальная защита"
                    snackbarHostState.showSnackbar("Активирован режим: $modeName")
                }
            },
            onDismiss = { showShieldSheet = false }
        )
    }

    // Tabs Manager Sheet
    if (showTabsSheet) {
        TabsManagerSheet(
            sheetState = tabsSheetState,
            tabs = tabs,
            activeTabId = activeTabId,
            onSelectTab = { viewModel.selectTab(it) },
            onCloseTab = { viewModel.closeTab(it) },
            onNewTab = { viewModel.addNewTab("https://ya.ru") },
            onCloseAllTabs = { viewModel.closeAllTabs() },
            onDismiss = { showTabsSheet = false }
        )
    }

    // History Sheet
    if (showHistorySheet) {
        HistorySheet(
            sheetState = historySheetState,
            historyList = historyList,
            onNavigate = { viewModel.navigateTo(it) },
            onDeleteEntry = { viewModel.deleteHistoryEntry(it) },
            onClearAll = { viewModel.clearAllHistory() },
            onDismiss = { showHistorySheet = false }
        )
    }

    // Bookmarks Sheet
    if (showBookmarksSheet) {
        BookmarksSheet(
            sheetState = bookmarksSheetState,
            bookmarks = bookmarksList,
            onNavigate = { viewModel.navigateTo(it) },
            onDeleteBookmark = { viewModel.deleteBookmark(it) },
            onDismiss = { showBookmarksSheet = false }
        )
    }

    // Password Vault Dialog
    if (showPasswordsSheet) {
        val domain = try {
            Uri.parse(currentUrl).host.orEmpty()
        } catch (_: Exception) {
            ""
        }

        PasswordVaultDialog(
            sheetState = passwordsSheetState,
            credentials = credentialsList,
            currentDomain = domain,
            onInsertIntoPage = { user, pass ->
                viewModel.requestAutofill(user, pass)
                coroutineScope.launch {
                    snackbarHostState.showSnackbar("Данные расшифрованы и вставлены в форму")
                }
            },
            onSaveCredential = { dom, user, pass ->
                viewModel.saveCredential(dom, user, pass)
                coroutineScope.launch {
                    snackbarHostState.showSnackbar("Пароль зашифрован (AES-256) и сохранен")
                }
            },
            onDeleteCredential = { viewModel.deleteCredential(it) },
            onDismiss = { showPasswordsSheet = false }
        )
    }

    // Protection Settings Dialog
    if (showSettingsSheet) {
        ProtectionSettingsDialog(
            sheetState = settingsSheetState,
            currentDnsMode = dnsMode,
            isDntEnabled = doNotTrack,
            isVulkanEnabled = vulkanAcceleration,
            isWebRtcProtectionEnabled = webrtcProtection,
            isXferwenProtectEnabled = xferwenProtect,
            isV8OptimizationEnabled = v8EngineOptimization,
            isAdHiderEnabled = adHiderEnabled,
            currentSearchEngine = searchEngine,
            isAutoSavePasswordsEnabled = autoSavePasswords,
            isSyntexSearchArchEnabled = useSyntexSearchArch,
            onDnsModeChange = { newMode ->
                viewModel.setDnsMode(newMode)
            },
            onToggleDnt = { viewModel.setDoNotTrack(it) },
            onToggleVulkan = { viewModel.setVulkanAcceleration(it) },
            onToggleWebRtc = { viewModel.setWebRtcProtection(it) },
            onToggleXferwenProtect = { viewModel.setXferwenProtect(it) },
            onToggleV8Optimization = { viewModel.setV8EngineOptimization(it) },
            onToggleAdHider = { viewModel.setAdHiderEnabled(it) },
            onToggleAutoSavePasswords = { viewModel.setAutoSavePasswords(it) },
            onToggleSyntexSearchArch = { viewModel.setUseSyntexSearchArch(it) },
            onSearchEngineChange = { viewModel.setSearchEngine(it) },
            onClearCacheAndCookies = {
                viewModel.clearCacheAndCookies()
                coroutineScope.launch {
                    snackbarHostState.showSnackbar("Кэш, куки и история сессии очищены")
                }
            },
            onMakeDefaultBrowser = onMakeDefaultBrowser,
            onShareApk = onShareApk,
            onDismiss = { showSettingsSheet = false }
        )
    }

    // Auto-save password offer prompt dialog
    pendingPasswordSave?.let { (domain, username, plainPass) ->
        SavePasswordOfferDialog(
            domain = domain,
            username = username,
            plainPassword = plainPass,
            onSave = { dom, user, pass ->
                viewModel.saveCredential(dom, user, pass)
                coroutineScope.launch {
                    snackbarHostState.showSnackbar("Пароль для $dom сохранен в зашифрованное хранилище")
                }
                pendingPasswordSave = null
            },
            onDismiss = {
                pendingPasswordSave = null
            }
        )
    }

    // 3-step Onboarding Wizard for fresh launches
    if (!hasCompletedOnboarding) {
        OnboardingWizardDialog(
            initialMode = dnsMode,
            initialSearchEngine = searchEngine,
            onComplete = { chosenMode, chosenEngine, useSyntex ->
                viewModel.setDnsMode(chosenMode)
                viewModel.setSearchEngine(chosenEngine)
                viewModel.setUseSyntexSearchArch(useSyntex)
                viewModel.setHasCompletedOnboarding(true)
                viewModel.goHome()
            }
        )
    }
}
