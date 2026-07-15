import Foundation

enum YouTubeEmbedHTMLBuilder {
    static func html(for media: ApprovedMediaItem) -> String {
        let embedURL: String

        switch media.mediaType {
        case .video:
            embedURL = "https://www.youtube.com/embed/\(media.youtubeID)?playsinline=1&autoplay=1&rel=0"
        case .playlist:
            embedURL = "https://www.youtube.com/embed/videoseries?list=\(media.youtubeID)&playsinline=1&autoplay=1&rel=0"
        }

        return """
        <!DOCTYPE html>
        <html>
        <head>
            <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no">
            <style>
                html, body {
                    margin: 0;
                    padding: 0;
                    background: #000;
                    width: 100%;
                    height: 100%;
                    overflow: hidden;
                }

                iframe {
                    border: 0;
                    width: 100%;
                    height: 100%;
                }
            </style>
        </head>
        <body>
            <iframe
                src="\(embedURL)"
                title="KidView Player"
                allow="autoplay; encrypted-media; picture-in-picture"
                allowfullscreen>
            </iframe>
        </body>
        </html>
        """
    }
}
