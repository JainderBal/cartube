package com.jay.cartube

import android.annotation.SuppressLint
import android.content.Context
import android.content.MutableContextWrapper
import android.graphics.Color
import android.view.View
import android.view.ViewGroup
import android.webkit.CookieManager
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout
import java.net.URLEncoder

/**
 * Holds the single WebView shown on the car screen. It survives the car surface being
 * destroyed/recreated (e.g. when the Search screen is pushed), so playback and page state
 * are kept. Its context is swapped to whichever Presentation currently owns it.
 */
object CarBrowser {
    const val HOME_URL = "https://m.youtube.com"

    private var webView: WebView? = null
    private var contextWrapper: MutableContextWrapper? = null
    private var pendingUrl: String? = null

    /** Container on the car display; used to show YouTube's fullscreen player on top. */
    var host: FrameLayout? = null
    private var fullscreenView: View? = null
    private var fullscreenCallback: WebChromeClient.CustomViewCallback? = null

    fun attach(displayContext: Context): WebView {
        webView?.let { existing ->
            contextWrapper?.baseContext = displayContext
            (existing.parent as? ViewGroup)?.removeView(existing)
            pendingUrl?.let { existing.loadUrl(it); pendingUrl = null }
            return existing
        }
        val wrapper = MutableContextWrapper(displayContext)
        contextWrapper = wrapper
        val created = create(wrapper)
        webView = created
        created.loadUrl(pendingUrl ?: HOME_URL)
        pendingUrl = null
        return created
    }

    fun detach() {
        exitFullscreen()
        webView?.let { (it.parent as? ViewGroup)?.removeView(it) }
        host = null
        CookieManager.getInstance().flush()
    }

    fun load(url: String) {
        val wv = webView
        if (wv == null) pendingUrl = url else wv.loadUrl(url)
    }

    fun search(query: String) {
        load("$HOME_URL/results?search_query=" + URLEncoder.encode(query, "UTF-8"))
    }

    fun home() = load(HOME_URL)

    fun goBack() {
        if (fullscreenView != null) { exitFullscreen(); return }
        webView?.let { if (it.canGoBack()) it.goBack() }
    }

    fun scrollBy(dx: Float, dy: Float) {
        webView?.scrollBy(dx.toInt(), dy.toInt())
    }

    fun zoom(factor: Float) {
        webView?.zoomBy(factor.coerceIn(0.5f, 2f))
    }

    private fun exitFullscreen() {
        val view = fullscreenView ?: return
        host?.removeView(view)
        fullscreenView = null
        fullscreenCallback?.onCustomViewHidden()
        fullscreenCallback = null
        webView?.visibility = View.VISIBLE
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun create(context: Context): WebView {
        CookieManager.getInstance().setAcceptCookie(true)
        return WebView(context).apply {
            setBackgroundColor(Color.BLACK)
            settings.apply {
                javaScriptEnabled = true
                domStorageEnabled = true
                mediaPlaybackRequiresUserGesture = false
                useWideViewPort = true
                loadWithOverviewMode = true
                builtInZoomControls = true
                displayZoomControls = false
            }
            CookieManager.getInstance().setAcceptThirdPartyCookies(this, true)

            webViewClient = object : WebViewClient() {
                override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
                    // Keep everything inside the WebView; block intent:// and app deep links
                    // that would try to open the YouTube app on the phone instead.
                    val scheme = request.url.scheme ?: return true
                    return scheme != "http" && scheme != "https"
                }
            }

            webChromeClient = object : WebChromeClient() {
                override fun onShowCustomView(view: View, callback: CustomViewCallback) {
                    val container = host
                    if (container == null) { callback.onCustomViewHidden(); return }
                    exitFullscreen()
                    fullscreenView = view
                    fullscreenCallback = callback
                    container.addView(
                        view,
                        FrameLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )
                    )
                    this@apply.visibility = View.INVISIBLE
                }

                override fun onHideCustomView() = exitFullscreen()
            }
        }
    }
}
