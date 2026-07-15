import SwiftUI
import WebKit

struct YouTubePlayerView: UIViewRepresentable {
    let media: ApprovedMediaItem

    func makeUIView(context: Context) -> WKWebView {
        let configuration = WKWebViewConfiguration()
        configuration.allowsInlineMediaPlayback = true
        configuration.mediaTypesRequiringUserActionForPlayback = []

        let webView = WKWebView(frame: .zero, configuration: configuration)
        webView.scrollView.isScrollEnabled = false
        webView.isOpaque = false
        webView.backgroundColor = .black
        webView.loadHTMLString(YouTubeEmbedHTMLBuilder.html(for: media), baseURL: URL(string: "https://www.youtube.com"))
        return webView
    }

    func updateUIView(_ webView: WKWebView, context: Context) {
        webView.loadHTMLString(YouTubeEmbedHTMLBuilder.html(for: media), baseURL: URL(string: "https://www.youtube.com"))
    }
}
