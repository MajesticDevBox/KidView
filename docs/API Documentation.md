# API Documentation

## Overview

KidView currently has no public backend API. The project is still in a local-first phase where the core application behavior is driven by on-device models, local storage, and embedded YouTube playback.

This document covers:

- current internal app contracts
- current local data structures
- planned future service boundaries

## Current External Dependencies

### YouTube Embed Playback

Current and planned usage:

- Android uses embedded YouTube playback through `WebView`
- iOS uses embedded YouTube playback through `WKWebView`

Purpose:

- play approved video IDs and playlist IDs only

Current limitation:

- playback is embed-based, not a custom video transport layer

### Optional Future YouTube Data API

Future use cases:

- validate video metadata
- fetch titles and thumbnails
- import playlist details
- inspect embeddability and made-for-kids metadata where needed

## Current Internal Domain Contracts

### ApprovedMediaItem

Purpose:

- the canonical model for a saved approved item

Fields:

- `id`: local unique identifier
- `mediaType`: `video` or `playlist`
- `youtubeID`: canonical YouTube video or playlist ID
- `originalURL`: original pasted URL
- `displayTitle`: parent-facing title override
- `displaySubtitle`: optional supporting label
- `contextNote`: optional parent note
- `expectedItemCount`: optional playlist size hint
- `thumbnailURL`: optional artwork URL
- `playlistEntries`: optional imported playlist entries
- `addedAt`: timestamp of item creation

### PlaylistVideoEntry

Purpose:

- represent an imported or cached video inside a playlist

Fields:

- `id`
- `youtubeID`
- `originalURL`
- `displayTitle`
- `displaySubtitle`
- `thumbnailURL`
- `addedAt`

### AppSettings

Purpose:

- platform-local application state snapshot

Core fields:

- `hasParentPin`
- `approvedMedia`
- `selectedApprovedMediaItemID`
- `timeLimitMinutes`

## URL Parsing Contract

### Input

- raw YouTube URL pasted by a parent

### Output

- `ParsedYouTubeMedia`

Fields:

- `mediaType`
- `youtubeID`

Supported sources:

- `youtube.com/watch?v=...`
- `youtube.com/watch?list=...`
- `youtube.com/playlist?list=...`
- `youtu.be/...`
- `youtube.com/shorts/...`
- `youtube.com/embed/...`
- `youtube.com/live/...`

Rejected inputs:

- unsupported hosts
- blank strings
- malformed URLs
- supported hosts without a usable media identifier

## Local Storage Contracts

### Android

Current storage approach:

- DataStore-backed local persistence

Stores:

- parent PIN state
- approved media list
- selected media
- lock preferences
- time limits

### iOS

Current scaffold approach:

- `UserDefaults` for approved media and simple state
- hashed parent PIN in `UserDefaults` as a temporary starter implementation

Planned improvement:

- move PIN-related material into Keychain

## Player Contracts

### Start Child Mode

Input:

- a selected `ApprovedMediaItem`

Expected behavior:

- present child-mode UI
- load the approved video or playlist
- maintain a minimal viewing surface

### Stop Child Mode

Input:

- parent-authenticated exit in the final design

Current status:

- still simplified in scaffolding and placeholder flows

## Planned Future Service Layer

If KidView adds a private optional backend later, recommended service areas are:

- account-free backup and restore
- device sync for approved playlists
- metadata caching
- remote parent controls

Suggested future endpoints:

- `POST /v1/media/parse`
- `POST /v1/media/import`
- `GET /v1/media/{id}`
- `POST /v1/playlists`
- `PATCH /v1/playlists/{id}`
- `POST /v1/session/start`
- `POST /v1/session/stop`

These are only placeholders and should not be implemented until a clear need exists.

## Design Rules For Future APIs

- local-first should remain the default
- private family use should not require sign-in
- no child profile tracking by default
- avoid collecting watch history unless explicitly desired
- keep approved media portable across platforms

## Versioning Guidance

For any future backend:

- use semantic API versioning such as `/v1/`
- document contract changes in this file
- keep mobile models backward-compatible where practical

## Current Gaps

- no formal API schema files yet
- no shared OpenAPI spec
- no backend endpoints are implemented today
- no signed content-import pipeline yet
