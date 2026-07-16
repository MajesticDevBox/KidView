package com.mdev.kidview.feature_player.playback

object YouTubeEmbedHtmlBuilder {
    fun buildVideoHtml(videoId: String): String = buildPlaylistHtml(listOf(videoId))

    fun buildHostedPlaylistHtml(playlistId: String): String {
        val safePlaylistId = sanitizeVideoId(playlistId).orEmpty()
        return """
            <!DOCTYPE html>
            <html>
            <head>
              <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no" />
              <style>
                html, body {
                  margin: 0;
                  padding: 0;
                  width: 100%;
                  height: 100%;
                  background: #000000;
                  overflow: hidden;
                }
                iframe {
                  position: absolute;
                  inset: 0;
                  width: 100%;
                  height: 100%;
                  border: 0;
                }
              </style>
            </head>
            <body>
              <iframe
                src="https://www.youtube-nocookie.com/embed/videoseries?list=$safePlaylistId&autoplay=1&playsinline=1&rel=0&fs=0&iv_load_policy=3&disablekb=1&controls=1"
                allow="autoplay; encrypted-media"
                referrerpolicy="strict-origin-when-cross-origin"
                allowfullscreen>
              </iframe>
            </body>
            </html>
        """.trimIndent()
    }

    fun buildPlaylistHtml(videoIds: List<String>): String {
        val safeVideoIds = videoIds.mapNotNull(::sanitizeVideoId).distinct()
        val firstVideoId = safeVideoIds.firstOrNull().orEmpty()
        val playlistArray = safeVideoIds.joinToString(separator = ",") { "\"$it\"" }
        val isSingleVideo = safeVideoIds.size <= 1

        val playerVars = buildString {
            append("autoplay: 1")
            append(", playsinline: 1")
            append(", rel: 0")
            append(", fs: 0")
            append(", iv_load_policy: 3")
            append(", disablekb: 1")
            append(", controls: 1")
            if (isSingleVideo && firstVideoId.isNotBlank()) {
                append(", loop: 1")
                append(", playlist: \"$firstVideoId\"")
            }
        }

        return """
            <!DOCTYPE html>
            <html>
            <head>
              <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no" />
              <style>
                html, body {
                  margin: 0;
                  padding: 0;
                  width: 100%;
                  height: 100%;
                  background: #000000;
                  overflow: hidden;
                }
                .frame {
                  position: absolute;
                  inset: 0;
                  width: 100%;
                  height: 100%;
                  border: 0;
                }
              </style>
            </head>
            <body>
              <div id="player" class="frame"></div>
              <script>
                var tag = document.createElement('script');
                tag.src = "https://www.youtube.com/player_api";
                var firstScriptTag = document.getElementsByTagName('script')[0];
                firstScriptTag.parentNode.insertBefore(tag, firstScriptTag);

                var playlistIds = [$playlistArray];
                var player;

                function onYouTubePlayerAPIReady() {
                  player = new YT.Player('player', {
                    host: 'https://www.youtube-nocookie.com',
                    videoId: '${firstVideoId}',
                    playerVars: { $playerVars },
                    events: {
                      onReady: function(event) {
                        if (playlistIds.length > 1) {
                          event.target.loadPlaylist(playlistIds, 0, 0);
                        } else {
                          event.target.playVideo();
                        }
                      }
                    }
                  });
                }
              </script>
            </body>
            </html>
        """.trimIndent()
    }

    private fun sanitizeVideoId(rawVideoId: String): String? {
        val safeVideoId = rawVideoId.filter { it.isLetterOrDigit() || it == '_' || it == '-' }
        return safeVideoId.takeIf { it.isNotBlank() }
    }
}
