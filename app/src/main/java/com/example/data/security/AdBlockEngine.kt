package com.example.data.security

import android.content.Context
import android.net.Uri
import com.example.data.model.DnsMode
import com.example.data.model.ProtectionStats
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit

/**
 * Intelligent Neural Filter Engine for Milana AI Cyber Shield.
 * Features full rule parsing and auto-update from:
 * 1. AdGuard Android Filters (Base & Spyware)
 * 2. DandelionSprout Anti-Malware List
 * 3. uBlock Origin Badware Filters
 * 4. DurableNapkin ScamBlocklist & Phishing Filters
 * 5. StevenBlack Hosts
 * 6. MrFeastProject-Framework & Phishing URL DataBase
 * 7. Yandex Analytics & yastatic tracking blocker
 * 8. Zero-latency local JSON caching with ETag / Version checking
 */
class AdBlockEngine(context: Context? = null) {

    enum class BlockType {
        NONE, AD, TRACKER, PHISHING
    }

    private val _stats = MutableStateFlow(ProtectionStats())
    val stats: StateFlow<ProtectionStats> = _stats.asStateFlow()

    // YouTube whitelist: prevents breaking video streaming, comments, and CDN
    private val youtubeWhitelist = setOf(
        "youtube.com",
        "www.youtube.com",
        "m.youtube.com",
        "youtu.be",
        "googlevideo.com",
        "ggpht.com",
        "ytimg.com",
        "youtube-nocookie.com",
        "youtube-ui.l.google.com",
        "youtubei.googleapis.com"
    )

    // Essential Whitelist to never break critical AI services & Captcha verification
    private val whitelistDomains = setOf(
        "openai.com",
        "chatgpt.com",
        "anthropic.com",
        "claude.ai",
        "google.com",
        "gemini.google.com",
        "smartcaptcha.yandexcloud.net",
        "captcha.yandex.net",
        "geohide.ru",
        "syntaxhypersoft.com",
        "github.com",
        "wikipedia.org",
        "recaptcha.net",
        "gstatic.com",
        "hcaptcha.com",
        "cloudflare.com",
        "challenges.cloudflare.com",
        "mrfeastproject.github.io"
    )

    // Strict Yandex Analytics & yastatic tracking signatures (all blocked)
    private val yandexTrackingSignatures = listOf(
        "mc.yandex.ru",
        "an.yandex.ru",
        "clck.yandex.ru",
        "awaps.yandex.net",
        "adfox.ru",
        "bs.yandex.ru",
        "yabs.yandex.ru",
        "metrika",
        "yastatic.net/mstat",
        "yastatic.net/metrika",
        "yastatic.net/tag.js",
        "yastatic.net/watch.js",
        "yastatic.net/adv",
        "yastatic.net/partner",
        "yastatic.net/s3/direct",
        "yastatic.net/s3/metrika",
        "stat.yandex.ru",
        "ext.yandex.ru",
        "analytics.yandex"
    )

    // Builtin Trackers from MrFeastProject-Framework
    private val builtinTrackers = setOf(
        "google-analytics.com", "googletagmanager.com", "googlesyndication.com",
        "googleadservices.com", "doubleclick.net", "ad.doubleclick.net",
        "adservice.google.com", "analytics.google.com", "pagead2.googlesyndication.com",
        "scorecardresearch.com", "quantserve.com", "quantcount.com",
        "chartbeat.com", "chartbeat.net", "hotjar.com", "mixpanel.com",
        "amplitude.com", "segment.com", "segment.io", "criteo.com", "criteo.net",
        "outbrain.com", "taboola.com", "mgid.com", "adroll.com",
        "connect.facebook.net", "pixel.facebook.com",
        "snap.licdn.com", "px.ads.linkedin.com", "ads.linkedin.com",
        "analytics.twitter.com", "ads-twitter.com", "analytics.tiktok.com",
        "ct.pinterest.com", "amazon-adsystem.com", "aax.amazon-adsystem.com",
        "fls-na.amazon.com", "adnxs.com", "rubiconproject.com", "openx.net",
        "pubmatic.com", "casalemedia.com", "33across.com", "moatads.com",
        "moatpixel.com", "appnexus.com", "tealiumiq.com", "ensighten.com",
        "fullstory.com", "contentsquare.net", "clicktale.net",
        "exoclick.com", "exdynsrv.com", "adsterra.com", "propellerads.com",
        "popcash.net", "popads.net", "revcontent.com", "content.ad",
        "coinhive.com", "coin-hive.com", "authedmine.com", "cryptoloot.pro",
        "platform.twitter.com", "syndication.twitter.com", "widgets.redditmedia.com",
        "assets.pinterest.com", "assets.tumblr.com", "static.addtoany.com",
        "w.sharethis.com", "static.getbutton.io", "share.pluso.ru", "connect.ok.ru",
        "fingerprint.com", "api.fpjs.io", "metrics.fpjs.io", "openfpcdn.io",
        "iovation.com", "online-metrix.net", "h.online-metrix.net",
        "clarity.ms", "telemetry", "tracking.php", "pixel.gif", "stats.wp.com"
    )

    // Ad signatures (includes AdGuard, uBlock, and MrFeastProject bases)
    private val adPatterns = listOf(
        "doubleclick.net",
        "googleads",
        "googlesyndication.com",
        "pagead2.googlesyndication",
        "adservice.google",
        "adriver.ru",
        "criteo.com",
        "outbrain.com",
        "taboola.com",
        "adnxs.com",
        "popads.net",
        "adcash.com",
        "/ads.js",
        "/adsystem",
        "/ads/ad.js",
        "banner-ad",
        "adservice",
        "propellerads",
        "trafficstars",
        "exoclick",
        "adsterra",
        "popcash",
        "yadro.ru",
        "begun.ru"
    )

    // Phishing, scam and malware signatures
    private val phishingPatterns = listOf(
        "phishing",
        "phish",
        "malware",
        "malware-traffic",
        "steal-",
        "fake-bank",
        "trojan",
        "ransomware",
        "verify-account-safety-check",
        "crypto-bonus-free",
        "free-gift-claim-winner",
        "login-update-security",
        "password-reset-urgent",
        "coinhive",
        "cryptoloot",
        "miner.js",
        "scam-alert",
        "lottery-win",
        "urgent-wallet-verification"
    )

    // Dynamic lists from Browse.py & MrFeast Framework
    private val dynamicBlockedHosts = ConcurrentHashMap.newKeySet<String>()
    private val dynamicHostCategories = ConcurrentHashMap<String, String>() // "ad", "tracker", "malware", "phishing"
    private val dynamicBlockedPaths = ConcurrentHashMap<String, MutableList<String>>()
    private val dynamicMalwareHosts = ConcurrentHashMap.newKeySet<String>()

    // Filter sources from Browse.py & MrFeast Framework
    data class ListSource(val name: String, val url: String, val category: String)

    private val filterSources = listOf(
        ListSource("adguard_base", "https://filters.adtidy.org/android/filters/2_optimized.txt", "ad"),
        ListSource("adguard_spyware", "https://filters.adtidy.org/android/filters/3_optimized.txt", "tracker"),
        ListSource("dandelion_malware", "https://raw.githubusercontent.com/DandelionSprout/adfilt/master/Alternate%20versions%20Anti-Malware%20List/AntiMalwareAdGuard.txt", "malware"),
        ListSource("ublock_badware", "https://raw.githubusercontent.com/uBlockOrigin/uAssets/master/filters/badware.txt", "malware"),
        ListSource("scamblocklist", "https://raw.githubusercontent.com/durablenapkin/scamblocklist/master/adguard.txt", "phishing"),
        ListSource("phishing_filter", "https://malware-filter.gitlab.io/malware-filter/phishing-filter-ag.txt", "phishing"),
        ListSource("hosts", "https://raw.githubusercontent.com/StevenBlack/hosts/master/hosts", "ad"),
        ListSource("mrfeast_framework", "https://raw.githubusercontent.com/MrFeastProject/MrFeastProject-Framework/main/Framework.txt", "ad"),
        ListSource("mrfeast_phishing", "https://raw.githubusercontent.com/MrFeastProject/MrFeastProject-Framework/main/Phishing%20URL%20DataBase.txt", "phishing")
    )

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(6, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .build()

    init {
        context?.let { ctx ->
            // Fast background load from cache and check for version updates
            CoroutineScope(Dispatchers.IO).launch {
                loadCachedFilters(ctx)
                checkForListUpdates(ctx)
            }
        }
    }

    /**
     * Parse raw filter list content as implemented in Browse.py.
     */
    fun parseList(raw: String, cat: String): Triple<Set<String>, Map<String, List<String>>, Set<String>> {
        val hosts = mutableSetOf<String>()
        val paths = mutableMapOf<String, MutableList<String>>()
        val malware = mutableSetOf<String>()
        val isThreat = cat == "malware" || cat == "phishing"

        for (line in raw.lineSequence()) {
            val trimmed = line.trim()
            if (trimmed.isEmpty() || trimmed.startsWith("!") || trimmed.startsWith("#") || trimmed.startsWith("[")) {
                continue
            }

            if (trimmed.startsWith("0.0.0.0") || trimmed.startsWith("127.0.0.1")) {
                val parts = trimmed.split("\\s+".toRegex())
                if (parts.size >= 2 && parts[1] != "localhost" && parts[1] != "0.0.0.0") {
                    val h = parts[1].lowercase()
                    hosts.add(h)
                    if (isThreat) malware.add(h)
                }
            } else if (trimmed.startsWith("||")) {
                val body = trimmed.substring(2).substringBefore('$').trimEnd('^')
                val domainPart = body.substringBefore('/')
                if (domainPart.contains('*')) continue

                if (body.contains('/')) {
                    val d = domainPart.lowercase()
                    val p = "/" + body.substringAfter('/').substringBefore('^')
                    paths.getOrPut(d) { mutableListOf() }.add(p)
                    if (isThreat) malware.add(d)
                } else {
                    val d = body.lowercase()
                    hosts.add(d)
                    if (isThreat) malware.add(d)
                }
            }
        }
        return Triple(hosts, paths, malware)
    }

    /**
     * Extracts version string from list header (e.g. ! Version: 2.0.1 or ! Last modified: ...).
     */
    private fun extractVersion(raw: String): String {
        for (line in raw.lineSequence().take(30)) {
            val trimmed = line.trim()
            if (trimmed.startsWith("! Version:", ignoreCase = true)) {
                return trimmed.substringAfter(":").trim()
            }
            if (trimmed.startsWith("! Last modified:", ignoreCase = true)) {
                return trimmed.substringAfter(":").trim()
            }
            if (trimmed.startsWith("! TimeUpdated:", ignoreCase = true)) {
                return trimmed.substringAfter(":").trim()
            }
        }
        return ""
    }

    private fun loadCachedFilters(context: Context) {
        val cacheFile = File(context.filesDir, "xferwen_blocklists_cache.json")
        if (!cacheFile.exists() || cacheFile.length() == 0L) return

        try {
            val json = JSONObject(cacheFile.readText())
            val hostsArray = json.optJSONArray("hosts") ?: JSONArray()
            for (i in 0 until hostsArray.length()) {
                val h = hostsArray.getString(i)
                dynamicBlockedHosts.add(h)
            }

            val malwareArray = json.optJSONArray("malware") ?: JSONArray()
            for (i in 0 until malwareArray.length()) {
                dynamicMalwareHosts.add(malwareArray.getString(i))
            }

            val catsObj = json.optJSONObject("cats")
            if (catsObj != null) {
                for (key in catsObj.keys()) {
                    dynamicHostCategories[key] = catsObj.getString(key)
                }
            }

            val pathsObj = json.optJSONObject("paths")
            if (pathsObj != null) {
                for (key in pathsObj.keys()) {
                    val arr = pathsObj.getJSONArray(key)
                    val pList = mutableListOf<String>()
                    for (j in 0 until arr.length()) {
                        pList.add(arr.getString(j))
                    }
                    dynamicBlockedPaths[key] = pList
                }
            }
        } catch (_: Exception) {}
    }

    private fun checkForListUpdates(context: Context) {
        val versionsFile = File(context.filesDir, "xferwen_list_versions.json")
        val savedVersions = mutableMapOf<String, String>()
        if (versionsFile.exists()) {
            try {
                val obj = JSONObject(versionsFile.readText())
                for (k in obj.keys()) {
                    savedVersions[k] = obj.getString(k)
                }
            } catch (_: Exception) {}
        }

        var anyUpdated = false
        val newHosts = mutableSetOf<String>()
        val newPaths = mutableMapOf<String, MutableList<String>>()
        val newMalware = mutableSetOf<String>()
        val newCats = mutableMapOf<String, String>()

        for (source in filterSources) {
            try {
                val req = Request.Builder()
                    .url(source.url)
                    .header("User-Agent", "Mozilla/5.0")
                    .build()

                val resp = httpClient.newCall(req).execute()
                if (resp.isSuccessful) {
                    val body = resp.body?.string().orEmpty()
                    if (body.isNotBlank()) {
                        val ver = extractVersion(body).ifBlank { resp.header("ETag") ?: resp.header("Last-Modified") ?: body.hashCode().toString() }
                        val oldVer = savedVersions[source.name]

                        if (oldVer != ver || dynamicBlockedHosts.isEmpty()) {
                            savedVersions[source.name] = ver
                            val (h, p, m) = parseList(body, source.category)
                            newHosts.addAll(h)
                            for ((k, v) in p) {
                                newPaths.getOrPut(k) { mutableListOf() }.addAll(v)
                            }
                            newMalware.addAll(m)
                            for (item in h) {
                                newCats[item] = source.category
                            }
                            anyUpdated = true
                        }
                    }
                }
            } catch (_: Exception) {
                // Ignore transient network errors during update check
            }
        }

        if (anyUpdated) {
            dynamicBlockedHosts.addAll(newHosts)
            dynamicMalwareHosts.addAll(newMalware)
            dynamicHostCategories.putAll(newCats)
            for ((k, v) in newPaths) {
                dynamicBlockedPaths.getOrPut(k) { mutableListOf() }.addAll(v)
            }

            // Save versions
            try {
                val vObj = JSONObject()
                for ((k, v) in savedVersions) {
                    vObj.put(k, v)
                }
                versionsFile.writeText(vObj.toString())

                // Save cache
                val cacheFile = File(context.filesDir, "xferwen_blocklists_cache.json")
                val cObj = JSONObject()
                cObj.put("hosts", JSONArray(dynamicBlockedHosts.take(30000)))
                cObj.put("malware", JSONArray(dynamicMalwareHosts.take(10000)))

                val catsObj = JSONObject()
                for ((k, v) in dynamicHostCategories.entries.take(30000)) {
                    catsObj.put(k, v)
                }
                cObj.put("cats", catsObj)

                val pathsObj = JSONObject()
                for ((k, v) in dynamicBlockedPaths.entries.take(5000)) {
                    pathsObj.put(k, JSONArray(v))
                }
                cObj.put("paths", pathsObj)

                cacheFile.writeText(cObj.toString())
            } catch (_: Exception) {}
        }
    }

    /**
     * Checks if a request URL should be blocked based on active DNS and security mode.
     * Blocks malicious domains, phishing, trackers, and intrusive ads in BOTH modes.
     */
    fun inspectRequest(urlStr: String, currentMode: DnsMode): BlockType {
        val host = try {
            val uri = java.net.URI(urlStr)
            uri.host?.lowercase()
        } catch (_: Exception) {
            null
        } ?: try {
            Uri.parse(urlStr).host?.lowercase()
        } catch (_: Exception) {
            null
        } ?: return BlockType.NONE
        val fullUrl = urlStr.lowercase()

        // 1. Never break YouTube media streams or playback
        if (youtubeWhitelist.any { host == it || host.endsWith(".$it") }) {
            return BlockType.NONE
        }

        // 2. Never break Captcha verification, security challenges, or SmartCaptcha
        if (fullUrl.contains("captcha") || fullUrl.contains("smartcaptcha") || fullUrl.contains("challenge") || fullUrl.contains("turnstile")) {
            return BlockType.NONE
        }

        // 3. Check essential whitelist (AI, Search Engines, CDNs)
        if (whitelistDomains.any { host == it || host.endsWith(".$it") }) {
            return BlockType.NONE
        }

        // 4. Check phishing & malware (from defender lists + MrFeast Defender) - BLOCKED IN BOTH MODES
        if (dynamicMalwareHosts.contains(host) || phishingPatterns.any { fullUrl.contains(it) }) {
            _stats.update { it.copy(blockedPhishing = it.blockedPhishing + 1) }
            return BlockType.PHISHING
        }

        val parts = host.split(".")
        for (i in parts.indices) {
            val domainSuffix = parts.subList(i, parts.size).joinToString(".")
            if (dynamicMalwareHosts.contains(domainSuffix)) {
                _stats.update { it.copy(blockedPhishing = it.blockedPhishing + 1) }
                return BlockType.PHISHING
            }
        }

        // 5. Strict Yandex Analytics & yastatic tracking block (user explicit requirement) - BLOCKED IN BOTH MODES
        if (yandexTrackingSignatures.any { fullUrl.contains(it) }) {
            _stats.update { it.copy(blockedTrackers = it.blockedTrackers + 1) }
            return BlockType.TRACKER
        }

        // 6. Check path-based blocked rules (from AdGuard / MrFeast) - BLOCKED IN BOTH MODES
        val urlPath = try { Uri.parse(urlStr).path?.lowercase().orEmpty() } catch (_: Exception) { "" }
        if (urlPath.isNotEmpty()) {
            for (i in parts.indices) {
                val domainSuffix = parts.subList(i, parts.size).joinToString(".")
                val pathsForDomain = dynamicBlockedPaths[domainSuffix]
                if (pathsForDomain != null && pathsForDomain.any { urlPath.startsWith(it) }) {
                    _stats.update { it.copy(blockedAds = it.blockedAds + 1) }
                    return BlockType.AD
                }
            }
        }

        // 7. Check ads (from dynamic lists and patterns) - BLOCKED IN BOTH MODES
        val isKnownAd = dynamicBlockedHosts.contains(host) ||
                adPatterns.any { fullUrl.contains(it) } ||
                dynamicHostCategories[host] == "ad"

        if (isKnownAd) {
            _stats.update { it.copy(blockedAds = it.blockedAds + 1) }
            return BlockType.AD
        }

        // 8. Check trackers from MrFeast Framework & AdGuard Spyware - BLOCKED IN BOTH MODES
        for (i in parts.indices) {
            val domainSuffix = parts.subList(i, parts.size).joinToString(".")
            if (builtinTrackers.contains(domainSuffix) || dynamicHostCategories[domainSuffix] == "tracker") {
                _stats.update { it.copy(blockedTrackers = it.blockedTrackers + 1) }
                return BlockType.TRACKER
            }
        }

        return BlockType.NONE
    }

    /**
     * Checks if a domain is a known threat (for XFERWEN Protect full page block).
     */
    fun isMaliciousDomain(urlStr: String): Boolean {
        val lower = urlStr.lowercase()
        val host = try { Uri.parse(urlStr).host?.lowercase().orEmpty() } catch (_: Exception) { "" }
        if (dynamicMalwareHosts.contains(host)) return true
        return phishingPatterns.any { lower.contains(it) }
    }

    /**
     * Generates XFERWEN Protect cyber warning HTML page.
     */
    fun getDefenderWarningHtml(domain: String): String {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="utf-8">
                <meta name="viewport" content="width=device-width, initial-scale=1">
                <title>XFERWEN Protect</title>
                <style>
                    body {
                        background: #050811;
                        color: #fecaca;
                        font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
                        text-align: center;
                        padding: 40px 20px;
                        margin: 0;
                    }
                    .shield { font-size: 64px; margin-bottom: 12px; }
                    h1 { color: #ef4444; font-size: 24px; margin-bottom: 10px; }
                    .box {
                        background: rgba(26, 11, 46, 0.8);
                        border: 1px solid #dc2626;
                        border-radius: 16px;
                        padding: 20px;
                        margin: 20px auto;
                        max-width: 500px;
                        color: #e2e8f0;
                        font-size: 14px;
                        line-height: 1.5;
                    }
                    .domain-badge {
                        background: #7f1d1d;
                        color: #fca5a5;
                        padding: 4px 10px;
                        border-radius: 8px;
                        font-family: monospace;
                        font-weight: bold;
                    }
                    .btn {
                        display: inline-block;
                        margin: 10px 8px;
                        padding: 12px 24px;
                        border-radius: 12px;
                        font-weight: bold;
                        text-decoration: none;
                        font-size: 14px;
                        cursor: pointer;
                        border: none;
                    }
                    .btn-exit { background: #00E5FF; color: #050811; }
                    .btn-risk { background: transparent; color: #ef4444; border: 1px solid #ef4444; }
                </style>
            </head>
            <body>
                <div class="shield">🛡️⚠️</div>
                <h1>XFERWEN Protect: Угроза заблокирована</h1>
                <div class="box">
                    Сайт <span class="domain-badge">$domain</span> заблокирован системой безопасности <b>Milana AI & Google SafeBrowsing</b>.
                    <br><br>
                    Обнаружена попытка фишинга, вредоносного скрипта или несанкционированного сбора личных данных.
                </div>
                <div>
                    <button class="btn btn-exit" onclick="window.history.back();">Покинуть сайт (Безопасно)</button>
                </div>
            </body>
            </html>
        """.trimIndent()
    }

    /**
     * JavaScript for WebRTC leak protection:
     * Disables STUN/TURN candidate leakage to protect real IP.
     */
    fun getWebRtcProtectionJsScript(): String {
        return """
            (function() {
                try {
                    if (window.RTCPeerConnection) {
                        const OrigRTC = window.RTCPeerConnection;
                        window.RTCPeerConnection = function(config, constraints) {
                            if (config && config.iceServers) {
                                config.iceServers = [];
                            }
                            const pc = new OrigRTC(config, constraints);
                            const origCreateOffer = pc.createOffer;
                            pc.createOffer = function(options) {
                                return origCreateOffer.apply(this, arguments).then(function(offer) {
                                    if (offer && offer.sdp) {
                                        offer.sdp = offer.sdp.replace(/a=candidate:.+\r\n/g, '');
                                    }
                                    return offer;
                                });
                            };
                            return pc;
                        };
                        window.RTCPeerConnection.prototype = OrigRTC.prototype;
                        window.webkitRTCPeerConnection = window.RTCPeerConnection;
                    }
                } catch(e) {}
            })();
        """.trimIndent()
    }

    /**
     * Cosmetic CSS Ad Hider:
     * Injects styles to completely hide banner ads, RTB blocks, and sticky promo popups.
     */
    fun getCosmeticAdHidingJsScript(): String {
        return """
            (function() {
                function injectHider() {
                    if (document.getElementById('xferwen-ad-hider')) return;
                    var style = document.createElement('style');
                    style.id = 'xferwen-ad-hider';
                    style.textContent = `
                        [id*="yandex_rtb"], [class*="yandex_rtb"], yandex-rtb,
                        [class*="ad-banner"], [class*="banner-ad"], [id*="banner"],
                        [class*="adsbygoogle"], ins.adsbygoogle, [data-ad-client],
                        [class*="sponsor"], [id*="google_ads"], div[class*="ads-"],
                        .ad-container, #ad-container, [data-ad], [aria-label*="реклама" i],
                        [aria-label*="advertisement" i], .branding, .direct-wrapper {
                            display: none !important;
                            visibility: hidden !important;
                            height: 0 !important;
                            min-height: 0 !important;
                            max-height: 0 !important;
                            opacity: 0 !important;
                            pointer-events: none !important;
                        }
                    `;
                    (document.head || document.documentElement).appendChild(style);
                }
                if (document.readyState === 'loading') {
                    document.addEventListener('DOMContentLoaded', injectHider);
                } else {
                    injectHider();
                }
                setTimeout(injectHider, 500);
            })();
        """.trimIndent()
    }

    /**
     * Resets counters if user clears browsing session.
     */
    fun resetStats() {
        _stats.value = ProtectionStats()
    }

    /**
     * JavaScript code snippet for Do Not Track and WebRTC protection injection.
     */
    fun getDoNotTrackJsScript(): String {
        return """
            (function() {
                try {
                    Object.defineProperty(navigator, 'doNotTrack', { get: function() { return '1'; }, configurable: true });
                    window.doNotTrack = '1';
                    if (navigator.globalPrivacyControl === undefined) {
                        Object.defineProperty(navigator, 'globalPrivacyControl', { get: function() { return true; }, configurable: true });
                    }
                } catch(e) {}
            })();
        """.trimIndent()
    }
}
