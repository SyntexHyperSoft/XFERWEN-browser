package com.example.ui.browser

import android.annotation.SuppressLint
import android.content.Context
import android.view.ViewGroup
import android.webkit.WebView
import java.util.concurrent.ConcurrentHashMap

/**
 * Singleton manager to preserve WebView state, DOM trees, and scroll positions across tabs.
 * Prevents active tabs from losing their state when the user opens or switches between tabs.
 */
object TabWebViewManager {
    private val webViewMap = ConcurrentHashMap<String, WebView>()

    @SuppressLint("SetJavaScriptEnabled")
    fun getOrCreate(tabId: String, context: Context, configure: (WebView) -> Unit): WebView {
        return webViewMap.getOrPut(tabId) {
            WebView(context).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                configure(this)
            }
        }
    }

    fun get(tabId: String): WebView? = webViewMap[tabId]

    fun remove(tabId: String): WebView? {
        val wv = webViewMap.remove(tabId)
        wv?.let {
            try {
                it.stopLoading()
                it.loadUrl("about:blank")
                it.clearHistory()
                (it.parent as? ViewGroup)?.removeView(it)
                it.destroy()
            } catch (_: Throwable) {}
        }
        return wv
    }

    fun clearAll() {
        for ((_, wv) in webViewMap) {
            try {
                wv.stopLoading()
                wv.destroy()
            } catch (_: Throwable) {}
        }
        webViewMap.clear()
    }
}
