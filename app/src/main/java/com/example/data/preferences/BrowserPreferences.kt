package com.example.data.preferences

import android.content.Context
import android.content.SharedPreferences
import com.example.data.model.DnsMode
import com.example.data.model.SearchEngine
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class BrowserPreferences(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("xferwen_settings_prefs", Context.MODE_PRIVATE)

    private val _dnsMode = MutableStateFlow(loadDnsMode())
    val dnsMode: StateFlow<DnsMode> = _dnsMode.asStateFlow()

    private val _doNotTrack = MutableStateFlow(prefs.getBoolean(KEY_DNT, true))
    val doNotTrack: StateFlow<Boolean> = _doNotTrack.asStateFlow()

    private val _vulkanAcceleration = MutableStateFlow(prefs.getBoolean(KEY_VULKAN, true))
    val vulkanAcceleration: StateFlow<Boolean> = _vulkanAcceleration.asStateFlow()

    private val _webrtcProtection = MutableStateFlow(prefs.getBoolean(KEY_WEBRTC, true))
    val webrtcProtection: StateFlow<Boolean> = _webrtcProtection.asStateFlow()

    private val _xferwenProtect = MutableStateFlow(prefs.getBoolean(KEY_XFERWEN_PROTECT, true))
    val xferwenProtect: StateFlow<Boolean> = _xferwenProtect.asStateFlow()

    private val _v8EngineOptimization = MutableStateFlow(prefs.getBoolean(KEY_V8_ENGINE, true))
    val v8EngineOptimization: StateFlow<Boolean> = _v8EngineOptimization.asStateFlow()

    private val _adHiderEnabled = MutableStateFlow(prefs.getBoolean(KEY_AD_HIDER, true))
    val adHiderEnabled: StateFlow<Boolean> = _adHiderEnabled.asStateFlow()

    private val _searchEngine = MutableStateFlow(loadSearchEngine())
    val searchEngine: StateFlow<SearchEngine> = _searchEngine.asStateFlow()

    private val _desktopMode = MutableStateFlow(prefs.getBoolean(KEY_DESKTOP_MODE, false))
    val desktopMode: StateFlow<Boolean> = _desktopMode.asStateFlow()

    private val _autoSavePasswords = MutableStateFlow(prefs.getBoolean(KEY_AUTO_SAVE_PASSWORDS, true))
    val autoSavePasswords: StateFlow<Boolean> = _autoSavePasswords.asStateFlow()

    private val _hasCompletedOnboarding = MutableStateFlow(prefs.getBoolean(KEY_HAS_COMPLETED_ONBOARDING, false))
    val hasCompletedOnboarding: StateFlow<Boolean> = _hasCompletedOnboarding.asStateFlow()

    private val _useSyntexSearchArch = MutableStateFlow(prefs.getBoolean(KEY_USE_SYNTEX_SEARCH_ARCH, false))
    val useSyntexSearchArch: StateFlow<Boolean> = _useSyntexSearchArch.asStateFlow()

    fun setDnsMode(mode: DnsMode) {
        prefs.edit().putString(KEY_DNS_MODE, mode.name).apply()
        _dnsMode.value = mode
    }

    fun setDoNotTrack(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_DNT, enabled).apply()
        _doNotTrack.value = enabled
    }

    fun setVulkanAcceleration(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_VULKAN, enabled).apply()
        _vulkanAcceleration.value = enabled
    }

    fun setWebRtcProtection(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_WEBRTC, enabled).apply()
        _webrtcProtection.value = enabled
    }

    fun setXferwenProtect(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_XFERWEN_PROTECT, enabled).apply()
        _xferwenProtect.value = enabled
    }

    fun setV8EngineOptimization(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_V8_ENGINE, enabled).apply()
        _v8EngineOptimization.value = enabled
    }

    fun setAdHiderEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_AD_HIDER, enabled).apply()
        _adHiderEnabled.value = enabled
    }

    fun setSearchEngine(engine: SearchEngine) {
        prefs.edit().putString(KEY_SEARCH_ENGINE, engine.name).apply()
        _searchEngine.value = engine
    }

    fun setDesktopMode(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_DESKTOP_MODE, enabled).apply()
        _desktopMode.value = enabled
    }

    fun setAutoSavePasswords(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_AUTO_SAVE_PASSWORDS, enabled).apply()
        _autoSavePasswords.value = enabled
    }

    fun setHasCompletedOnboarding(completed: Boolean) {
        prefs.edit().putBoolean(KEY_HAS_COMPLETED_ONBOARDING, completed).apply()
        _hasCompletedOnboarding.value = completed
    }

    fun setUseSyntexSearchArch(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_USE_SYNTEX_SEARCH_ARCH, enabled).apply()
        _useSyntexSearchArch.value = enabled
    }

    private fun loadDnsMode(): DnsMode {
        val name = prefs.getString(KEY_DNS_MODE, DnsMode.SYNTEXT_BYPASS.name)
        return try {
            DnsMode.valueOf(name ?: DnsMode.SYNTEXT_BYPASS.name)
        } catch (_: Exception) {
            DnsMode.SYNTEXT_BYPASS
        }
    }

    private fun loadSearchEngine(): SearchEngine {
        val name = prefs.getString(KEY_SEARCH_ENGINE, SearchEngine.XFERWEN_FAS.name)
        return try {
            SearchEngine.valueOf(name ?: SearchEngine.XFERWEN_FAS.name)
        } catch (_: Exception) {
            SearchEngine.XFERWEN_FAS
        }
    }

    companion object {
        private const val KEY_DNS_MODE = "pref_dns_mode"
        private const val KEY_DNT = "pref_do_not_track"
        private const val KEY_VULKAN = "pref_vulkan_accel"
        private const val KEY_WEBRTC = "pref_webrtc_protection"
        private const val KEY_XFERWEN_PROTECT = "pref_xferwen_protect"
        private const val KEY_V8_ENGINE = "pref_v8_engine"
        private const val KEY_AD_HIDER = "pref_ad_hider"
        private const val KEY_SEARCH_ENGINE = "pref_search_engine"
        private const val KEY_DESKTOP_MODE = "pref_desktop_mode"
        private const val KEY_AUTO_SAVE_PASSWORDS = "pref_auto_save_passwords"
        private const val KEY_HAS_COMPLETED_ONBOARDING = "pref_has_completed_onboarding"
        private const val KEY_USE_SYNTEX_SEARCH_ARCH = "pref_use_syntex_search_arch"
    }
}
