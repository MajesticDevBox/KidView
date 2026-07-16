package com.mdev.kidview.feature_player.playback

import android.annotation.SuppressLint
import android.graphics.Color
import android.view.ViewGroup
import android.webkit.WebResourceError
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun YouTubeVideoPlayer(
    videoIds: List<String>,
    playlistId: String? = null,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val html = remember(videoIds, playlistId) {
        when {
            !playlistId.isNullOrBlank() -> YouTubeEmbedHtmlBuilder.buildHostedPlaylistHtml(playlistId)
            else -> YouTubeEmbedHtmlBuilder.buildPlaylistHtml(videoIds)
        }
    }
    val webView = remember {
        WebView(context).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT,
            )
            setBackgroundColor(Color.BLACK)
            overScrollMode = WebView.OVER_SCROLL_NEVER
            isHorizontalScrollBarEnabled = false
            isVerticalScrollBarEnabled = false
            isLongClickable = false
            setOnLongClickListener { true }
            isHapticFeedbackEnabled = false
            setDownloadListener { _, _, _, _, _ -> }
            webChromeClient = WebChromeClient()
            webViewClient = object : WebViewClient() {
                override fun shouldOverrideUrlLoading(
                    view: WebView?,
                    request: WebResourceRequest?,
                ): Boolean {
                    return request?.isForMainFrame == true
                }

                override fun onReceivedError(
                    view: WebView?,
                    request: WebResourceRequest?,
                    error: WebResourceError?,
                ) {
                    super.onReceivedError(view, request, error)
                    if (request?.isForMainFrame == true) {
                        view?.loadData(
                            "<html><body style='background:black;color:white;font-family:sans-serif;padding:24px;'>Playback could not be loaded. Use parent unlock and try again.</body></html>",
                            "text/html",
                            "utf-8",
                        )
                    }
                }
            }
            settings.apply {
                javaScriptEnabled = true
                domStorageEnabled = true
                mediaPlaybackRequiresUserGesture = false
                cacheMode = WebSettings.LOAD_DEFAULT
                builtInZoomControls = false
                displayZoomControls = false
                allowFileAccess = false
                allowContentAccess = false
                javaScriptCanOpenWindowsAutomatically = false
                setSupportMultipleWindows(false)
                loadsImagesAutomatically = true
                mixedContentMode = WebSettings.MIXED_CONTENT_NEVER_ALLOW
                safeBrowsingEnabled = true
                setSupportZoom(false)
            }
        }
    }

    LaunchedEffect(webView, html) {
        webView.onResume()
        webView.loadDataWithBaseURL(
            "https://www.youtube-nocookie.com",
            html,
            "text/html",
            "utf-8",
            null,
        )
    }

    AndroidView(
        modifier = modifier,
        factory = { webView },
        update = { },
    )

    DisposableEffect(webView) {
        onDispose {
            webView.onPause()
            webView.loadUrl("about:blank")
            webView.stopLoading()
            webView.destroy()
        }
    }
}
