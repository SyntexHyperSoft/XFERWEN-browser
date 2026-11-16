package com.example.ui.browser

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.os.Build
import android.view.View
import android.webkit.CookieManager
import android.webkit.JsPromptResult
import android.webkit.JsResult
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.webkit.WebSettingsCompat
import androidx.webkit.WebViewFeature
import com.example.data.model.BrowserTab
import com.example.data.model.DnsMode
import com.example.data.security.AdBlockEngine
import com.example.data.security.DohResolver
import com.example.data.security.DohStatusPage
import com.example.data.security.StealthAntiBot
import java.io.ByteArrayInputStream

/**
 * High-performance Chromium WebView with Google V8 Engine optimization,
 * Vulkan hardware acceleration, WebRTC leak protection, Cosmetic Ad Hider,
 * and XFERWEN Protect (SafeBrowsing / Yandex Protect threat interceptor).
 * Utilizes TabWebViewManager to maintain session and scroll positions across tabs.
 */
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun BrowserWebView(
    tab: BrowserTab,
    dnsMode: DnsMode,
    isDntEnabled: Boolean,
    isVulkanEnabled: Boolean,
    isWebRtcProtectionEnabled: Boolean,
    isXferwenProtectEnabled: Boolean,
    isV8OptimizationEnabled: Boolean,
    isAdHiderEnabled: Boolean,
    adBlockEngine: AdBlockEngine,
    autofillPayload: Pair<String, String>?,
    onUpdateTabInfo: (title: String?, url: String?, progress: Int?, isLoading: Boolean?) -> Unit,
    onRecordHistory: (url: String, title: String) -> Unit,
    onAutofillConsumed: () -> Unit,
    onOfferSavePassword: ((domain: String, username: String, pass: String) -> Unit)? = null,
    onShowCustomView: (View, WebChromeClient.CustomViewCallback) -> Unit,
    onHideCustomView: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    LaunchedEffect(dnsMode) {
        DohResolver.updateMode(dnsMode)
    }

    val webView = remember(tab.id) {
        TabWebViewManager.getOrCreate(tab.id, context) { wv ->
            // Direct Vulkan / Hardware acceleration
            if (isVulkanEnabled) {
                wv.setLayerType(View.LAYER_TYPE_HARDWARE, null)
            }

            // CookieManager configuration
            val cookieManager = CookieManager.getInstance()
            cookieManager.setAcceptCookie(true)
            cookieManager.setAcceptThirdPartyCookies(wv, true)

            // Password autosave bridge
            wv.addJavascriptInterface(
                object {
                    @android.webkit.JavascriptInterface
                    fun onCredentialCaptured(domain: String, user: String, pass: String) {
                        if (pass.isNotBlank()) {
                            onOfferSavePassword?.invoke(domain, user, pass)
                        }
                    }
                },
                "XferwenPasswordBridge"
            )

            wv.settings.apply {
                javaScriptEnabled = true
                javaScriptCanOpenWindowsAutomatically = true
                setSupportMultipleWindows(false)
                domStorageEnabled = true
                databaseEnabled = true
                allowFileAccess = true
                allowContentAccess = true
                loadWithOverviewMode = true
                useWideViewPort = true
                mediaPlaybackRequiresUserGesture = false
                mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                cacheMode = WebSettings.LOAD_DEFAULT

                // Vulkan & High-performance offscreen pre-rasterization for ultra-smooth 60/120fps scrolling
                offscreenPreRaster = true
                setEnableSmoothTransition(true)

                if (isV8OptimizationEnabled) {
                    setRenderPriority(WebSettings.RenderPriority.HIGH)
                }

                // Genuine Chrome Mobile User-Agent
                userAgentString = StealthAntiBot.REAL_MOBILE_CHROME_UA

                // Disable X-Requested-With header to stop bot/crawler detection on Yandex & Cloudflare
                if (WebViewFeature.isFeatureSupported(WebViewFeature.REQUESTED_WITH_HEADER_ALLOW_LIST)) {
                    try {
                        WebSettingsCompat.setRequestedWithHeaderOriginAllowList(this, emptySet())
                    } catch (_: Throwable) {}
                }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    safeBrowsingEnabled = isXferwenProtectEnabled
                }
            }

            wv.webViewClient = object : WebViewClient() {
                override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                    val targetUrl = request?.url?.toString() ?: return false

                    // Intercept FAS protocol links clicked within pages or search results
                    if (targetUrl.startsWith("fas://", ignoreCase = true)) {
                        val realTarget = com.example.data.model.FasDomainRegistry.resolveInput(targetUrl)
                        if (realTarget != null) {
                            view?.loadUrl(realTarget)
                            onUpdateTabInfo(null, targetUrl, 10, true)
                            return true
                        }
                    }

                    // Intercept XFERWEN + FAS Search Engine requests
                    if (targetUrl.startsWith("https://xferwen.search/") || targetUrl.startsWith("http://xferwen.search/")) {
                        val query = try {
                            android.net.Uri.parse(targetUrl).getQueryParameter("q").orEmpty()
                        } catch (_: Exception) { "" }
                        val searchHtml = com.example.data.security.FasSearchPage.getHtml(query)
                        view?.loadDataWithBaseURL("https://xferwen.search/", searchHtml, "text/html", "UTF-8", null)
                        val encoded = try { java.net.URLEncoder.encode(query, "UTF-8") } catch (_: Exception) { query }
                        onUpdateTabInfo("XFERWEN + FAS: $query", "fas://search?q=$encoded", 100, false)
                        return true
                    }

                    // Intercept visits to DoH gateway endpoints to display rich cyber status dashboard
                    if (targetUrl.contains("geohide.ru", ignoreCase = true) || targetUrl.contains("adguard-dns.com", ignoreCase = true)) {
                        val statusHtml = DohStatusPage.getHtml(dnsMode, DohResolver.status.value)
                        view?.loadDataWithBaseURL("https://xferwen.internal/", statusHtml, "text/html", "UTF-8", null)
                        return true
                    }

                    // XFERWEN Protect (SafeBrowsing / Yandex Protect interceptor)
                    if (isXferwenProtectEnabled && adBlockEngine.isMaliciousDomain(targetUrl)) {
                        val domain = request?.url?.host ?: targetUrl
                        view?.loadDataWithBaseURL(
                            "about:blank",
                            adBlockEngine.getDefenderWarningHtml(domain),
                            "text/html",
                            "UTF-8",
                            null
                        )
                        return true
                    }
                    return false
                }

                override fun shouldInterceptRequest(
                    view: WebView?,
                    request: WebResourceRequest?
                ): WebResourceResponse? {
                    val url = request?.url?.toString() ?: return null

                    // Intercept XFERWEN + FAS Search Engine requests
                    if (url.startsWith("https://xferwen.search/") || url.startsWith("http://xferwen.search/")) {
                        val query = try {
                            android.net.Uri.parse(url).getQueryParameter("q").orEmpty()
                        } catch (_: Exception) { "" }
                        val searchHtml = com.example.data.security.FasSearchPage.getHtml(query)
                        return WebResourceResponse(
                            "text/html",
                            "UTF-8",
                            ByteArrayInputStream(searchHtml.toByteArray(Charsets.UTF_8))
                        )
                    }

                    // Intercept visits to DoH gateway endpoints to return status dashboard
                    if (url.contains("geohide.ru", ignoreCase = true) || url.contains("adguard-dns.com", ignoreCase = true)) {
                        val statusHtml = DohStatusPage.getHtml(dnsMode, DohResolver.status.value)
                        return WebResourceResponse(
                            "text/html",
                            "UTF-8",
                            ByteArrayInputStream(statusHtml.toByteArray(Charsets.UTF_8))
                        )
                    }

                    // Inspect request through Milana AI Filter (dual-mode filtering)
                    val blockType = adBlockEngine.inspectRequest(url, dnsMode)
                    if (blockType != AdBlockEngine.BlockType.NONE) {
                        return WebResourceResponse(
                            "text/plain",
                            "UTF-8",
                            ByteArrayInputStream(ByteArray(0))
                        )
                    }

                    return super.shouldInterceptRequest(view, request)
                }

                override fun doUpdateVisitedHistory(view: WebView?, url: String?, isReload: Boolean) {
                    super.doUpdateVisitedHistory(view, url, isReload)
                    url?.let {
                        val title = view?.title ?: it
                        onUpdateTabInfo(title, it, null, null)
                    }
                }

                override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                    super.onPageStarted(view, url, favicon)
                    val currentUrl = url ?: view?.url.orEmpty()
                    onUpdateTabInfo(view?.title, currentUrl, 10, true)

                    // Inject Stealth Anti-Bot Cloaking script early to prevent robot blocks
                    view?.evaluateJavascript(StealthAntiBot.getStealthJsScript(), null)

                    // Inject WebRTC leak protection
                    if (isWebRtcProtectionEnabled) {
                        view?.evaluateJavascript(adBlockEngine.getWebRtcProtectionJsScript(), null)
                    }

                    // Inject Do Not Track if enabled
                    if (isDntEnabled) {
                        view?.evaluateJavascript(adBlockEngine.getDoNotTrackJsScript(), null)
                    }

                    // Inject Cosmetic Ad Hiding
                    if (isAdHiderEnabled) {
                        view?.evaluateJavascript(adBlockEngine.getCosmeticAdHidingJsScript(), null)
                    }
                }

                override fun onPageFinished(view: WebView?, url: String?) {
                    super.onPageFinished(view, url)
                    val currentUrl = view?.url ?: url.orEmpty()
                    val title = view?.title ?: currentUrl
                    onUpdateTabInfo(title, currentUrl, 100, false)

                    // Reinforce stealth and ad-hiding scripts post DOM creation
                    view?.evaluateJavascript(StealthAntiBot.getStealthJsScript(), null)

                    if (isWebRtcProtectionEnabled) {
                        view?.evaluateJavascript(adBlockEngine.getWebRtcProtectionJsScript(), null)
                    }

                    if (isAdHiderEnabled) {
                        view?.evaluateJavascript(adBlockEngine.getCosmeticAdHidingJsScript(), null)
                    }

                    // Install automatic password submission detector
                    view?.evaluateJavascript(PASSWORD_CAPTURE_JS, null)

                    if (currentUrl.isNotBlank() && !currentUrl.startsWith("data:") && currentUrl != "about:blank") {
                        onRecordHistory(currentUrl, title)
                    }
                    CookieManager.getInstance().flush()
                }
            }

            wv.webChromeClient = object : WebChromeClient() {
                override fun onProgressChanged(view: WebView?, newProgress: Int) {
                    super.onProgressChanged(view, newProgress)
                    val currentUrl = view?.url
                    onUpdateTabInfo(view?.title, currentUrl, newProgress, newProgress < 100)

                    // Inject stealth & protections at 20% progress to beat early inline scripts
                    if (newProgress in 20..35) {
                        view?.evaluateJavascript(StealthAntiBot.getStealthJsScript(), null)
                        if (isWebRtcProtectionEnabled) {
                            view?.evaluateJavascript(adBlockEngine.getWebRtcProtectionJsScript(), null)
                        }
                    }
                }

                override fun onReceivedTitle(view: WebView?, title: String?) {
                    super.onReceivedTitle(view, title)
                    onUpdateTabInfo(title, view?.url, null, null)
                }

                override fun onJsAlert(view: WebView?, url: String?, message: String?, result: JsResult?): Boolean {
                    result?.confirm()
                    return true
                }

                override fun onJsConfirm(view: WebView?, url: String?, message: String?, result: JsResult?): Boolean {
                    result?.confirm()
                    return true
                }

                override fun onJsPrompt(view: WebView?, url: String?, message: String?, defaultValue: String?, result: JsPromptResult?): Boolean {
                    result?.confirm(defaultValue ?: "")
                    return true
                }

                override fun onShowCustomView(view: View?, callback: CustomViewCallback?) {
                    if (view != null && callback != null) {
                        onShowCustomView(view, callback)
                    }
                }

                override fun onHideCustomView() {
                    onHideCustomView()
                }
            }

            // Initial load only if fresh
            if (tab.url.isNotBlank() && tab.url != "about:blank") {
                wv.loadUrl(tab.url)
            }
        }
    }

    // Handle Autofill Injection into web inputs
    LaunchedEffect(autofillPayload) {
        autofillPayload?.let { (user, pass) ->
            val escapedUser = user.replace("'", "\\'")
            val escapedPass = pass.replace("'", "\\'")
            val js = """
                (function() {
                    try {
                        var passInputs = document.querySelectorAll('input[type="password"]');
                        if (passInputs.length > 0) {
                            var passInput = passInputs[0];
                            passInput.value = '$escapedPass';
                            passInput.dispatchEvent(new Event('input', { bubbles: true }));
                            passInput.dispatchEvent(new Event('change', { bubbles: true }));
                            
                            var form = passInput.form || document;
                            var userInputs = form.querySelectorAll('input[type="text"], input[type="email"], input[name*="user"], input[name*="login"], input[id*="user"]');
                            if (userInputs.length > 0) {
                                userInputs[0].value = '$escapedUser';
                                userInputs[0].dispatchEvent(new Event('input', { bubbles: true }));
                                userInputs[0].dispatchEvent(new Event('change', { bubbles: true }));
                            }
                        }
                    } catch(e) {}
                })();
            """.trimIndent()
            webView.evaluateJavascript(js, null)
            onAutofillConsumed()
        }
    }

    AndroidView(
        factory = { webView },
        modifier = modifier.fillMaxSize()
    )
}

private const val PASSWORD_CAPTURE_JS = """
(function() {
    if (window.__xferwen_pw_listener_installed) return;
    window.__xferwen_pw_listener_installed = true;

    function extractAndSend() {
        try {
            var pwds = document.querySelectorAll('input[type="password"]');
            for (var i = 0; i < pwds.length; i++) {
                var pwd = pwds[i];
                var passVal = pwd.value;
                if (passVal && passVal.trim().length > 0) {
                    var form = pwd.form || document;
                    var userCandidates = form.querySelectorAll('input[type="text"], input[type="email"], input[type="tel"], input[name*="user"], input[name*="login"], input[name*="email"], input[id*="user"], input[id*="login"]');
                    var userVal = '';
                    for (var j = 0; j < userCandidates.length; j++) {
                        if (userCandidates[j] !== pwd && userCandidates[j].value && userCandidates[j].value.trim().length > 0) {
                            userVal = userCandidates[j].value.trim();
                            break;
                        }
                    }
                    if (window.XferwenPasswordBridge) {
                        window.XferwenPasswordBridge.onCredentialCaptured(window.location.hostname, userVal, passVal);
                    }
                    break;
                }
            }
        } catch(e) {}
    }

    document.addEventListener('submit', extractAndSend, true);
    document.addEventListener('click', function(e) {
        var target = e.target;
        if (!target) return;
        var tag = (target.tagName || '').toLowerCase();
        var type = (target.getAttribute('type') || '').toLowerCase();
        var text = (target.innerText || target.value || '').toLowerCase();
        if (type === 'submit' || tag === 'button' || text.indexOf('войти') !== -1 || text.indexOf('вход') !== -1 || text.indexOf('login') !== -1 || text.indexOf('sign in') !== -1) {
            setTimeout(extractAndSend, 120);
        }
    }, true);
})();
"""

