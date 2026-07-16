//
//  ContentView.swift
//  KidView
//
//  Created by Jareth Thomas on 7/15/26.
//

import CryptoKit
import Security
import SwiftUI
import WebKit

struct ContentView: View {
  @State private var store = ParentControlsStore()

  var body: some View {
    Group {
      if store.settings.hasParentPin {
        ParentRootView(store: store)
      } else {
        PinSetupView(store: store)
      }
    }
    .tint(KidViewTheme.accentYellow)
    .preferredColorScheme(.dark)
  }
}

@Observable
final class ParentControlsStore {
  private let defaultsKey = "kidView.settings.v1"

  var settings: AppSettings
  var selectedTab: ParentTab = .home
  var path: [AppRoute] = []

  init() {
    if let data = UserDefaults.standard.data(forKey: defaultsKey),
      let decoded = try? JSONDecoder().decode(AppSettings.self, from: data)
    {
      settings = decoded
    } else {
      settings = AppSettings()
    }

    installSeedMediaIfNeeded()
  }

  var selectedItem: ApprovedMediaItem? {
    let selectedId = settings.selectedApprovedMediaItemId
    return settings.approvedMedia.first { $0.localId == selectedId } ?? settings.approvedMedia.first
  }

  var canStartChildMode: Bool {
    guard let selectedItem else { return false }
    return PlaybackPolicy.support(for: selectedItem).isPlayableNow
  }

  func createParentPin(_ pin: String) {
    settings.parentPin = PinHasher.create(pin: pin)
    settings.hasParentPin = true
    save()
  }

  func verifyParentPin(_ pin: String) -> Bool {
    guard let parentPin = settings.parentPin else { return false }
    return PinHasher.verify(pin: pin, stored: parentPin)
  }

  func updateParentPin(currentPin: String, newPin: String) -> Bool {
    guard verifyParentPin(currentPin) else { return false }
    settings.parentPin = PinHasher.create(pin: newPin)
    save()
    return true
  }

  func saveMedia(_ item: ApprovedMediaItem) {
    if let index = settings.approvedMedia.firstIndex(where: { $0.localId == item.localId }) {
      settings.approvedMedia[index] = item
    } else {
      settings.approvedMedia.append(item)
    }
    settings.selectedApprovedMediaItemId = item.localId
    save()
  }

  func addVideoEntry(_ entry: PlaylistVideoEntry, toPlaylistId playlistId: String) {
    guard let index = settings.approvedMedia.firstIndex(where: { $0.localId == playlistId }) else {
      return
    }

    settings.approvedMedia[index].playlistEntries.append(entry)
    settings.approvedMedia[index].expectedItemCount =
      settings.approvedMedia[index].playlistEntries.count
    settings.selectedApprovedMediaItemId = playlistId
    save()
  }

  func deleteMedia(_ item: ApprovedMediaItem) {
    settings.approvedMedia.removeAll { $0.localId == item.localId }
    settings.selectedApprovedMediaItemId = settings.approvedMedia.first?.localId
    save()
  }

  func selectMedia(_ item: ApprovedMediaItem) {
    settings.selectedApprovedMediaItemId = item.localId
    save()
  }

  func updateTimeLimit(_ minutes: Int?) {
    settings.timeLimitMinutes = minutes
    save()
  }

  func updateLockMode(_ mode: LockMode) {
    settings.preferredLockMode = mode
    save()
  }

  private func save() {
    if let data = try? JSONEncoder().encode(settings) {
      UserDefaults.standard.set(data, forKey: defaultsKey)
    }
  }

  private func installSeedMediaIfNeeded() {
    let seedId = "seed-bluey-playlist"
    if settings.installedSeedMediaIds?.contains(seedId) == true {
      return
    }

    if settings.approvedMedia.contains(where: { item in
      item.localId == seedId || item.playlistEntries.contains { $0.youtubeId == "nszchmr9DFw" }
    }) {
      settings.installedSeedMediaIds = (settings.installedSeedMediaIds ?? []) + [seedId]
      save()
      return
    }

    let blueyURL = "https://www.youtube.com/embed/nszchmr9DFw?si=jtmTl0HQGCaL8s2W"
    let blueyItem = ApprovedMediaItem(
      localId: seedId,
      mediaType: .playlist,
      youtubeId: "",
      originalUrl: blueyURL,
      displayTitle: "Bluey",
      displaySubtitle: "Local playlist",
      contextNote: "Seeded from the YouTube link.",
      expectedItemCount: 1,
      thumbnailUrl: "",
      playlistEntries: [
        PlaylistVideoEntry(
          localId: "seed-bluey-video-nszchmr9DFw",
          youtubeId: "nszchmr9DFw",
          originalUrl: blueyURL,
          displayTitle: "Bluey",
          displaySubtitle: "YouTube video",
          thumbnailUrl: "",
          addedAt: Date()
        )
      ],
      addedAt: Date()
    )

    settings.approvedMedia.append(blueyItem)
    settings.selectedApprovedMediaItemId = settings.selectedApprovedMediaItemId ?? seedId
    settings.installedSeedMediaIds = (settings.installedSeedMediaIds ?? []) + [seedId]
    save()
  }
}

struct AppSettings: Codable {
  var hasParentPin = false
  var parentPin: StoredPinHash?
  var approvedMedia: [ApprovedMediaItem] = []
  var selectedApprovedMediaItemId: String?
  var preferredLockMode: LockMode = .standardPhone
  var timeLimitMinutes: Int?
  var installedSeedMediaIds: [String]?
}

struct ApprovedMediaItem: Codable, Identifiable, Hashable {
  var id: String { localId }

  var localId = UUID().uuidString
  var mediaType: MediaType
  var youtubeId: String
  var originalUrl: String
  var displayTitle: String
  var displaySubtitle: String
  var contextNote: String
  var expectedItemCount: Int?
  var thumbnailUrl: String
  var playlistEntries: [PlaylistVideoEntry]
  var addedAt: Date
}

struct PlaylistVideoEntry: Codable, Identifiable, Hashable {
  var id: String { localId }

  var localId = UUID().uuidString
  var youtubeId: String
  var originalUrl: String
  var displayTitle: String
  var displaySubtitle: String
  var thumbnailUrl: String
  var addedAt: Date
}

enum MediaType: String, Codable, CaseIterable {
  case video
  case playlist

  var label: String {
    switch self {
    case .video: "Video"
    case .playlist: "Playlist"
    }
  }
}

enum LockMode: String, Codable, CaseIterable {
  case standardPhone
  case dedicatedDevice

  var title: String {
    switch self {
    case .standardPhone: "Standard phone"
    case .dedicatedDevice: "Dedicated-device preview"
    }
  }

  var description: String {
    switch self {
    case .standardPhone: "Parent-guided child mode inside this app."
    case .dedicatedDevice: "Preview branch for future stronger supervised-device behavior."
    }
  }
}

enum ParentTab: Hashable {
  case home
  case playlist
  case settings
}

enum AppRoute: Hashable {
  case addMedia(MediaType)
  case editMedia(ApprovedMediaItem)
  case timeLimits
  case handoff(ApprovedMediaItem)
  case childMode(ApprovedMediaItem)
}

enum VideoSaveTarget {
  case standalone
  case existingPlaylist
}

enum KidViewTheme {
  static let primaryBlue = Color(red: 0.118, green: 0.533, blue: 0.898)
  static let deepBlue = Color(red: 0.051, green: 0.278, blue: 0.631)
  static let accentYellow = Color(red: 1.0, green: 0.835, blue: 0.310)
  static let safetyGreen = Color(red: 0.298, green: 0.686, blue: 0.314)
  static let skyBlue = Color(red: 0.506, green: 0.831, blue: 0.980)
  static let backgroundNearBlack = Color(red: 0.043, green: 0.063, blue: 0.125)
  static let navyDeep = Color(red: 0.016, green: 0.063, blue: 0.133)
  static let navy = Color(red: 0.027, green: 0.086, blue: 0.184)
  static let surfaceNavy = Color(red: 0.067, green: 0.137, blue: 0.247)
  static let surfaceNavyAlt = Color(red: 0.082, green: 0.231, blue: 0.467)
  static let dividerBlueGray = Color(red: 0.165, green: 0.259, blue: 0.408)
  static let secondaryText = Color(red: 0.722, green: 0.780, blue: 0.878)
  static let mutedText = Color(red: 0.761, green: 0.843, blue: 1.0)
  static let primaryText = Color.white
  static let error = Color(red: 1.0, green: 0.541, blue: 0.502)

  static let backgroundGradient = LinearGradient(
    colors: [navyDeep, navy, deepBlue],
    startPoint: .top,
    endPoint: .bottom
  )

  static func roundedFont(_ style: Font.TextStyle, weight: Font.Weight = .regular) -> Font {
    .system(style, design: .rounded).weight(weight)
  }
}

struct PinSetupView: View {
  let store: ParentControlsStore

  @State private var pin = ""
  @State private var confirmPin = ""
  @State private var errorMessage: String?

  var body: some View {
    NavigationStack {
      AppBackground {
        VStack(spacing: 18) {
          Spacer()

          BrandHeader()
            .frame(maxWidth: .infinity, alignment: .leading)

          KidViewCard(cornerRadius: 28, padding: 22) {
            HStack(spacing: 16) {
              Image("KidViewBrandIcon")
                .resizable()
                .scaledToFit()
                .frame(width: 86, height: 86)
                .clipShape(RoundedRectangle(cornerRadius: 24))

              VStack(alignment: .leading, spacing: 8) {
                Text("Create Parent PIN")
                  .font(KidViewTheme.roundedFont(.title, weight: .black))
                  .foregroundStyle(KidViewTheme.primaryText)

                Text("Safe Videos. Happy Kids.")
                  .font(KidViewTheme.roundedFont(.headline, weight: .bold))
                  .foregroundStyle(KidViewTheme.accentYellow)
              }
            }

            Text("Use 4 to 8 digits. This PIN unlocks parent controls and exits child mode.")
              .font(KidViewTheme.roundedFont(.body))
              .foregroundStyle(KidViewTheme.secondaryText)

            VStack(spacing: 12) {
              SecureField("Parent PIN", text: $pin)
                .keyboardType(.numberPad)
                .textContentType(.oneTimeCode)
                .textFieldStyle(.roundedBorder)
                .onChange(of: pin) { _, newValue in pin = ParentPinValidator.sanitize(newValue) }

              SecureField("Confirm PIN", text: $confirmPin)
                .keyboardType(.numberPad)
                .textContentType(.oneTimeCode)
                .textFieldStyle(.roundedBorder)
                .onChange(of: confirmPin) { _, newValue in
                  confirmPin = ParentPinValidator.sanitize(newValue)
                }
            }

            if let errorMessage {
              Text(errorMessage)
                .font(KidViewTheme.roundedFont(.callout, weight: .semibold))
                .foregroundStyle(KidViewTheme.error)
            }

            Button {
              if let validation = ParentPinValidator.validateNewPin(
                pin: pin, confirmPin: confirmPin)
              {
                errorMessage = validation
              } else {
                store.createParentPin(pin)
              }
            } label: {
              Label("Save PIN", systemImage: "checkmark.circle.fill")
                .frame(maxWidth: .infinity)
            }
            .buttonStyle(PrimaryKidViewButtonStyle())
          }

          Spacer()
        }
        .padding(24)
      }
      .navigationTitle("KidView Lock")
    }
  }
}

struct ParentRootView: View {
  let store: ParentControlsStore

  var body: some View {
    @Bindable var store = store

    NavigationStack(path: $store.path) {
      Group {
        switch store.selectedTab {
        case .home:
          ParentHomeView(store: store)
        case .playlist:
          ParentPlaylistView(store: store)
        case .settings:
          ParentSettingsView(store: store)
        }
      }
      .navigationDestination(for: AppRoute.self) { route in
        switch route {
        case .addMedia(let type):
          AddMediaView(store: store, mode: type, editingItem: nil)
        case .editMedia(let item):
          AddMediaView(store: store, mode: item.mediaType, editingItem: item)
        case .timeLimits:
          TimeLimitsView(store: store)
        case .handoff(let item):
          ChildModeHandoffView(store: store, item: item)
        case .childMode(let item):
          ChildModeView(store: store, item: item)
        }
      }
    }
  }
}

struct ParentHomeView: View {
  let store: ParentControlsStore

  var body: some View {
    ParentShell(store: store, title: "Parent Mode", selectedTab: .home) {
      VStack(alignment: .leading, spacing: 14) {
        BrandHeader()

        Text("My Playlist")
          .font(KidViewTheme.roundedFont(.title2, weight: .black))
          .foregroundStyle(KidViewTheme.primaryText)

        if store.settings.approvedMedia.isEmpty {
          EmptyPlaylistView()
            .frame(maxHeight: .infinity)
        } else {
          ScrollView {
            LazyVStack(spacing: 10) {
              ForEach(Array(store.settings.approvedMedia.enumerated()), id: \.element.id) {
                index, item in
                MediaCard(
                  item: item,
                  index: index,
                  selected: item.localId == store.selectedItem?.localId,
                  onSelect: { store.selectMedia(item) },
                  onDelete: { store.deleteMedia(item) }
                )
              }
            }
          }
        }

        HStack {
          Button {
            store.path.append(.addMedia(.video))
          } label: {
            Label("Add Video", systemImage: "play.rectangle")
              .frame(maxWidth: .infinity)
          }
          .buttonStyle(SecondaryKidViewButtonStyle())

          Button {
            store.path.append(.addMedia(.playlist))
          } label: {
            Label("Add Playlist", systemImage: "list.bullet.rectangle")
              .frame(maxWidth: .infinity)
          }
          .buttonStyle(SecondaryKidViewButtonStyle())
        }

        Button {
          if let item = store.selectedItem {
            store.path.append(.handoff(item))
          }
        } label: {
          Label("Start Child Mode", systemImage: "lock.fill")
            .frame(maxWidth: .infinity)
        }
        .buttonStyle(PrimaryKidViewButtonStyle())
        .disabled(!store.canStartChildMode)
        .opacity(store.canStartChildMode ? 1 : 0.5)
      }
      .padding(16)
    }
  }
}

struct ParentPlaylistView: View {
  let store: ParentControlsStore

  var body: some View {
    ParentShell(store: store, title: "Playlist", selectedTab: .playlist) {
      ScrollView {
        LazyVStack(spacing: 12) {
          if store.settings.approvedMedia.isEmpty {
            EmptyPlaylistView()
          }

          ForEach(Array(store.settings.approvedMedia.enumerated()), id: \.element.id) {
            index, item in
            Button {
              store.path.append(.editMedia(item))
            } label: {
              KidViewCard {
                HStack(spacing: 12) {
                  MediaGlyph(type: item.mediaType, index: index)
                  VStack(alignment: .leading, spacing: 4) {
                    Text(MediaDisplay.title(item, index: index))
                      .font(KidViewTheme.roundedFont(.headline, weight: .bold))
                      .foregroundStyle(KidViewTheme.primaryText)
                    Text(
                      MediaDisplay.subtitle(
                        item, playbackStatus: PlaybackPolicy.support(for: item).statusLabel)
                    )
                    .font(KidViewTheme.roundedFont(.subheadline))
                    .foregroundStyle(KidViewTheme.secondaryText)
                  }
                  Spacer()
                  Button(role: .destructive) {
                    store.deleteMedia(item)
                  } label: {
                    Image(systemName: "trash")
                      .foregroundStyle(KidViewTheme.error)
                      .frame(width: 34, height: 34)
                  }
                  .buttonStyle(.plain)
                  .accessibilityLabel("Delete \(MediaDisplay.title(item, index: index))")
                  Image(systemName: "chevron.right")
                    .font(.caption.weight(.bold))
                    .foregroundStyle(KidViewTheme.accentYellow)
                }
              }
            }
            .buttonStyle(.plain)
          }
        }
        .padding(16)
      }
      .scrollContentBackground(.hidden)
      .toolbar {
        Menu {
          Button("Add Video", systemImage: "play.rectangle") {
            store.path.append(.addMedia(.video))
          }
          Button("Add Playlist", systemImage: "list.bullet.rectangle") {
            store.path.append(.addMedia(.playlist))
          }
        } label: {
          Image(systemName: "plus")
        }
      }
    }
  }
}

struct ParentSettingsView: View {
  let store: ParentControlsStore

  @State private var showingPinSheet = false

  var body: some View {
    ParentShell(store: store, title: "Settings", selectedTab: .settings) {
      ScrollView {
        VStack(spacing: 12) {
          KidViewCard {
            Label("Device setup", systemImage: "iphone")
              .font(KidViewTheme.roundedFont(.headline, weight: .bold))
              .foregroundStyle(KidViewTheme.primaryText)
            Text(
              "Use standard phone mode for family devices today. Dedicated-device behavior requires supervised-device capabilities outside a normal App Store app."
            )
            .font(KidViewTheme.roundedFont(.subheadline))
            .foregroundStyle(KidViewTheme.secondaryText)
          }

          KidViewCard {
            Button {
              store.path.append(.timeLimits)
            } label: {
              HStack {
                Label("Time limits", systemImage: "timer")
                  .font(KidViewTheme.roundedFont(.headline, weight: .bold))
                Spacer()
                Text(TimeLimitFormatter.summary(minutes: store.settings.timeLimitMinutes))
                  .foregroundStyle(KidViewTheme.accentYellow)
                  .font(KidViewTheme.roundedFont(.subheadline, weight: .bold))
              }
              .foregroundStyle(KidViewTheme.primaryText)
            }
            .buttonStyle(.plain)

            Divider()
              .background(KidViewTheme.dividerBlueGray)

            Button {
              showingPinSheet = true
            } label: {
              Label("Change Parent PIN", systemImage: "key.fill")
                .font(KidViewTheme.roundedFont(.headline, weight: .bold))
                .foregroundStyle(KidViewTheme.primaryText)
            }
            .buttonStyle(.plain)
          }

          KidViewCard {
            Text("Lock mode")
              .font(KidViewTheme.roundedFont(.headline, weight: .bold))
              .foregroundStyle(KidViewTheme.primaryText)
            ForEach(LockMode.allCases, id: \.self) { mode in
              Button {
                store.updateLockMode(mode)
              } label: {
                HStack(alignment: .top) {
                  Image(
                    systemName: store.settings.preferredLockMode == mode
                      ? "checkmark.circle.fill" : "circle"
                  )
                  .foregroundStyle(
                    store.settings.preferredLockMode == mode
                      ? KidViewTheme.accentYellow : KidViewTheme.secondaryText)
                  VStack(alignment: .leading) {
                    Text(mode.title)
                      .font(KidViewTheme.roundedFont(.subheadline, weight: .bold))
                      .foregroundStyle(KidViewTheme.primaryText)
                    Text(mode.description)
                      .font(KidViewTheme.roundedFont(.caption))
                      .foregroundStyle(KidViewTheme.secondaryText)
                  }
                }
                .padding(.vertical, 5)
              }
              .buttonStyle(.plain)
            }
          }

          KidViewCard {
            Label("Current guidance", systemImage: "shield.fill")
              .font(KidViewTheme.roundedFont(.headline, weight: .bold))
              .foregroundStyle(KidViewTheme.safetyGreen)
            Text(
              "Child mode can keep playback inside this app and require the parent PIN to return. iOS does not allow a regular app to disable Home, Control Center, or multitasking; use Guided Access from iOS Settings for stronger locking."
            )
            .font(KidViewTheme.roundedFont(.subheadline))
            .foregroundStyle(KidViewTheme.secondaryText)
          }
        }
        .padding(16)
      }
      .sheet(isPresented: $showingPinSheet) {
        ChangePinView(store: store)
          .presentationDetents([.medium, .large])
      }
    }
  }
}

struct AddMediaView: View {
  let store: ParentControlsStore
  let mode: MediaType
  let editingItem: ApprovedMediaItem?

  @Environment(\.dismiss) private var dismiss
  @State private var url = ""
  @State private var title = ""
  @State private var subtitle = ""
  @State private var note = ""
  @State private var saveTarget: VideoSaveTarget = .standalone
  @State private var selectedPlaylistId: String?
  @State private var errorMessage: String?

  private var availablePlaylists: [ApprovedMediaItem] {
    store.settings.approvedMedia.filter { $0.mediaType == .playlist }
  }

  var body: some View {
    AppBackground {
      ScrollView {
        VStack(spacing: 14) {
          KidViewCard(cornerRadius: 28) {
            HStack(spacing: 12) {
              MediaGlyph(type: mode, index: 0)
              VStack(alignment: .leading, spacing: 4) {
                Text(mode == .video ? "Video link" : "Playlist details")
                  .font(KidViewTheme.roundedFont(.title3, weight: .black))
                  .foregroundStyle(KidViewTheme.primaryText)
                Text(
                  mode == .video
                    ? "Paste a single YouTube video URL."
                    : "Paste a YouTube playlist URL, or leave it blank for a local playlist."
                )
                .font(KidViewTheme.roundedFont(.subheadline))
                .foregroundStyle(KidViewTheme.secondaryText)
              }
            }

            HStack(spacing: 10) {
              TextField(
                mode == .video ? "YouTube video URL" : "YouTube playlist URL (optional)",
                text: $url
              )
              .textInputAutocapitalization(.never)
              .keyboardType(.URL)
              .textFieldStyle(.roundedBorder)

              Button {
                pasteURL()
              } label: {
                Image(systemName: "doc.on.clipboard")
                  .frame(width: 42, height: 42)
              }
              .buttonStyle(.plain)
              .foregroundStyle(KidViewTheme.navyDeep)
              .background(KidViewTheme.accentYellow, in: RoundedRectangle(cornerRadius: 12))
              .accessibilityLabel("Paste link")
            }

            TextField(mode == .video ? "Video title" : "Playlist title", text: $title)
              .textFieldStyle(.roundedBorder)
            TextField(mode == .video ? "Channel or subtitle" : "Short subtitle", text: $subtitle)
              .textFieldStyle(.roundedBorder)

            TextField("Parent note", text: $note, axis: .vertical)
              .lineLimit(3...6)
              .textFieldStyle(.roundedBorder)
          }

          if mode == .video && editingItem == nil {
            KidViewCard {
              Text("Save this video")
                .font(KidViewTheme.roundedFont(.headline, weight: .bold))
                .foregroundStyle(KidViewTheme.primaryText)
              Text("Keep it as its own item, or attach it to one of your saved playlists.")
                .font(KidViewTheme.roundedFont(.subheadline))
                .foregroundStyle(KidViewTheme.secondaryText)

              VideoSaveTargetRow(
                title: "Standalone video",
                subtitle: "Shows up as its own item.",
                selected: saveTarget == .standalone,
                enabled: true
              ) {
                saveTarget = .standalone
              }

              VideoSaveTargetRow(
                title: "Add to existing playlist",
                subtitle: availablePlaylists.isEmpty
                  ? "Create a playlist first, then add videos here."
                  : "Adds this video into a saved playlist.",
                selected: saveTarget == .existingPlaylist,
                enabled: !availablePlaylists.isEmpty
              ) {
                saveTarget = .existingPlaylist
                selectedPlaylistId = selectedPlaylistId ?? availablePlaylists.first?.localId
              }

              if saveTarget == .existingPlaylist && !availablePlaylists.isEmpty {
                VStack(spacing: 8) {
                  ForEach(availablePlaylists) { playlist in
                    Button {
                      selectedPlaylistId = playlist.localId
                    } label: {
                      HStack {
                        Image(
                          systemName: selectedPlaylistId == playlist.localId
                            ? "checkmark.circle.fill" : "circle"
                        )
                        .foregroundStyle(
                          selectedPlaylistId == playlist.localId
                            ? KidViewTheme.accentYellow : KidViewTheme.secondaryText)
                        VStack(alignment: .leading, spacing: 2) {
                          Text(MediaDisplay.title(playlist))
                            .font(KidViewTheme.roundedFont(.subheadline, weight: .bold))
                            .foregroundStyle(KidViewTheme.primaryText)
                          Text(MediaDisplay.subtitle(playlist))
                            .font(KidViewTheme.roundedFont(.caption))
                            .foregroundStyle(KidViewTheme.secondaryText)
                        }
                        Spacer()
                      }
                      .padding(10)
                      .background(
                        KidViewTheme.surfaceNavyAlt.opacity(0.55),
                        in: RoundedRectangle(cornerRadius: 14))
                    }
                    .buttonStyle(.plain)
                  }
                }
              }
            }
          }

          if let errorMessage {
            KidViewCard {
              Label(errorMessage, systemImage: "exclamationmark.triangle.fill")
                .font(KidViewTheme.roundedFont(.callout, weight: .semibold))
                .foregroundStyle(KidViewTheme.error)
            }
          }

          Button {
            save()
          } label: {
            Label(
              editingItem == nil ? "Save \(mode.label)" : "Save Changes",
              systemImage: "checkmark.circle.fill"
            )
            .frame(maxWidth: .infinity)
          }
          .buttonStyle(PrimaryKidViewButtonStyle())
        }
        .padding(16)
      }
    }
    .navigationTitle(editingItem == nil ? "Add \(mode.label)" : "Edit \(mode.label)")
    .toolbarBackground(KidViewTheme.navy.opacity(0.96), for: .navigationBar)
    .toolbarColorScheme(.dark, for: .navigationBar)
    .onAppear {
      guard let editingItem else { return }
      url = editingItem.originalUrl
      title = editingItem.displayTitle
      subtitle = editingItem.displaySubtitle
      note = editingItem.contextNote
      selectedPlaylistId = availablePlaylists.first?.localId
    }
  }

  private func save() {
    let trimmedURL = url.trimmingCharacters(in: .whitespacesAndNewlines)
    let trimmedTitle = title.trimmingCharacters(in: .whitespacesAndNewlines)
    let parsed = trimmedURL.isEmpty ? nil : YouTubeURLParser.parse(trimmedURL)

    if mode == .video {
      guard let parsed, parsed.mediaType == .video else {
        errorMessage = "Paste a supported YouTube video link."
        return
      }

      if saveTarget == .existingPlaylist {
        guard let selectedPlaylistId else {
          errorMessage = "Choose a playlist for this video."
          return
        }

        let entry = PlaylistVideoEntry(
          youtubeId: parsed.youtubeId,
          originalUrl: trimmedURL,
          displayTitle: trimmedTitle,
          displaySubtitle: subtitle.trimmingCharacters(in: .whitespacesAndNewlines),
          thumbnailUrl: "",
          addedAt: Date()
        )
        store.addVideoEntry(entry, toPlaylistId: selectedPlaylistId)
        dismiss()
        return
      }

      saveItem(youtubeId: parsed.youtubeId, originalUrl: trimmedURL, displayTitle: trimmedTitle)
      return
    }

    if trimmedURL.isEmpty && trimmedTitle.isEmpty {
      errorMessage = "Name the local playlist, or paste a YouTube link."
      return
    }

    if !trimmedURL.isEmpty && parsed == nil {
      errorMessage = "Paste a supported YouTube link, or leave the link blank for a local playlist."
      return
    }

    if let parsed, parsed.mediaType == .video {
      let entry = PlaylistVideoEntry(
        youtubeId: parsed.youtubeId,
        originalUrl: trimmedURL,
        displayTitle: trimmedTitle,
        displaySubtitle: subtitle.trimmingCharacters(in: .whitespacesAndNewlines),
        thumbnailUrl: "",
        addedAt: Date()
      )
      saveItem(
        youtubeId: "",
        originalUrl: trimmedURL,
        displayTitle: trimmedTitle,
        playlistEntries: [entry]
      )
      return
    }

    saveItem(
      youtubeId: parsed?.youtubeId ?? "", originalUrl: trimmedURL, displayTitle: trimmedTitle)
  }

  private func saveItem(
    youtubeId: String,
    originalUrl: String,
    displayTitle: String,
    playlistEntries: [PlaylistVideoEntry]? = nil
  ) {
    let item = ApprovedMediaItem(
      localId: editingItem?.localId ?? UUID().uuidString,
      mediaType: mode,
      youtubeId: youtubeId,
      originalUrl: originalUrl,
      displayTitle: displayTitle,
      displaySubtitle: subtitle.trimmingCharacters(in: .whitespacesAndNewlines),
      contextNote: note.trimmingCharacters(in: .whitespacesAndNewlines),
      expectedItemCount: nil,
      thumbnailUrl: "",
      playlistEntries: playlistEntries ?? editingItem?.playlistEntries ?? [],
      addedAt: editingItem?.addedAt ?? Date()
    )
    store.saveMedia(item)
    dismiss()
  }

  private func pasteURL() {
    guard
      let pastedValue = UIPasteboard.general.string?.trimmingCharacters(
        in: .whitespacesAndNewlines
      ),
      !pastedValue.isEmpty
    else {
      errorMessage = "Clipboard does not contain a link."
      return
    }

    url = pastedValue
    errorMessage = nil
  }
}

struct TimeLimitsView: View {
  let store: ParentControlsStore

  @State private var minutes = 20.0
  @State private var enabled = false

  var body: some View {
    AppBackground {
      VStack(spacing: 18) {
        KidViewCard(cornerRadius: 30, padding: 22) {
          HStack {
            VStack(alignment: .leading, spacing: 6) {
              Text("Session timer")
                .font(KidViewTheme.roundedFont(.title2, weight: .black))
                .foregroundStyle(KidViewTheme.primaryText)
              Text("Support a clear ending without making the screen feel punitive.")
                .font(KidViewTheme.roundedFont(.subheadline))
                .foregroundStyle(KidViewTheme.secondaryText)
            }
            Spacer()
            Image(systemName: "timer")
              .font(.system(size: 34, weight: .bold))
              .foregroundStyle(KidViewTheme.accentYellow)
          }

          ZStack {
            Circle()
              .stroke(KidViewTheme.dividerBlueGray.opacity(0.9), lineWidth: 18)
            Circle()
              .trim(from: 0, to: enabled ? minutes / 60.0 : 0)
              .stroke(KidViewTheme.accentYellow, style: StrokeStyle(lineWidth: 18, lineCap: .round))
              .rotationEffect(.degrees(-90))

            VStack(spacing: 6) {
              Text(enabled ? TimeLimitFormatter.timerFace(minutes: Int(minutes)) : "--:--")
                .font(.system(size: 42, weight: .black, design: .rounded).monospacedDigit())
                .foregroundStyle(KidViewTheme.primaryText)
              Text(
                enabled ? TimeLimitFormatter.endAtLabel(minutes: Int(minutes)) : "No end time set"
              )
              .font(KidViewTheme.roundedFont(.caption, weight: .semibold))
              .foregroundStyle(KidViewTheme.secondaryText)
            }
          }
          .frame(width: 220, height: 220)
          .frame(maxWidth: .infinity)

          Toggle("Enable time limit", isOn: $enabled)
            .font(KidViewTheme.roundedFont(.headline, weight: .bold))
            .foregroundStyle(KidViewTheme.primaryText)
            .tint(KidViewTheme.accentYellow)

          if enabled {
            Slider(value: $minutes, in: 5...60, step: 5)
              .tint(KidViewTheme.accentYellow)
            HStack {
              Text("5 min")
              Spacer()
              Text("60 min")
            }
            .font(KidViewTheme.roundedFont(.caption, weight: .semibold))
            .foregroundStyle(KidViewTheme.secondaryText)
          }
        }

        Button {
          store.updateTimeLimit(enabled ? Int(minutes) : nil)
        } label: {
          Label("Save Time Limit", systemImage: "checkmark.circle.fill")
            .frame(maxWidth: .infinity)
        }
        .buttonStyle(PrimaryKidViewButtonStyle())

        Spacer()
      }
      .padding(16)
    }
    .navigationTitle("Time Limits")
    .toolbarBackground(KidViewTheme.navy.opacity(0.96), for: .navigationBar)
    .toolbarColorScheme(.dark, for: .navigationBar)
    .onAppear {
      enabled = store.settings.timeLimitMinutes != nil
      minutes = Double(store.settings.timeLimitMinutes ?? 20)
    }
  }
}

struct ChildModeHandoffView: View {
  let store: ParentControlsStore
  let item: ApprovedMediaItem

  var body: some View {
    AppBackground {
      VStack(spacing: 18) {
        Spacer()

        KidViewCard(cornerRadius: 30, padding: 22) {
          HStack(spacing: 14) {
            MediaGlyph(type: item.mediaType, index: 0)
            VStack(alignment: .leading, spacing: 6) {
              Text("Ready for Child Mode")
                .font(KidViewTheme.roundedFont(.title, weight: .black))
                .foregroundStyle(KidViewTheme.primaryText)
              Text(MediaDisplay.title(item))
                .font(KidViewTheme.roundedFont(.headline, weight: .bold))
                .foregroundStyle(KidViewTheme.accentYellow)
            }
          }

          Text(PlaybackPolicy.support(for: item).detailMessage)
            .font(KidViewTheme.roundedFont(.body))
            .foregroundStyle(KidViewTheme.secondaryText)

          if let limit = store.settings.timeLimitMinutes {
            StatusBadge(text: "\(limit) minute limit", color: KidViewTheme.accentYellow)
          }
        }

        Button {
          store.path.append(.childMode(item))
        } label: {
          Label("Hand Device To Child", systemImage: "lock.fill")
            .frame(maxWidth: .infinity)
        }
        .buttonStyle(PrimaryKidViewButtonStyle())

        Text("For stronger locking on iPhone, turn on Guided Access after playback starts.")
          .font(KidViewTheme.roundedFont(.footnote))
          .foregroundStyle(KidViewTheme.secondaryText)
          .multilineTextAlignment(.center)

        Spacer()
      }
      .padding(24)
    }
    .navigationTitle("Child Mode")
  }
}

struct ChildModeView: View {
  let store: ParentControlsStore
  let item: ApprovedMediaItem

  @Environment(\.dismiss) private var dismiss
  @State private var startDate = Date()
  @State private var now = Date()
  @State private var showingUnlock = false

  var body: some View {
    ZStack(alignment: .topTrailing) {
      Color.black.ignoresSafeArea()

      YouTubePlayerView(html: YouTubeHTMLBuilder.html(for: item))
        .ignoresSafeArea()

      VStack(alignment: .trailing, spacing: 10) {
        if let label = timeLimitLabel {
          Text(label)
            .font(.caption.monospacedDigit().weight(.bold))
            .padding(.horizontal, 12)
            .padding(.vertical, 7)
            .background(KidViewTheme.navyDeep.opacity(0.82), in: Capsule())
            .foregroundStyle(KidViewTheme.accentYellow)
            .overlay {
              Capsule()
                .stroke(KidViewTheme.accentYellow.opacity(0.35), lineWidth: 1)
            }
        }

        Button {
          showingUnlock = true
        } label: {
          Label("Parent", systemImage: "lock.fill")
            .labelStyle(.iconOnly)
            .padding(12)
            .background(KidViewTheme.navyDeep.opacity(0.82), in: Circle())
            .foregroundStyle(KidViewTheme.accentYellow)
            .overlay {
              Circle()
                .stroke(KidViewTheme.accentYellow.opacity(0.35), lineWidth: 1)
            }
        }
        .accessibilityLabel("Parent unlock")
      }
      .padding()

      if timeLimitReached {
        VStack(spacing: 16) {
          Image(systemName: "timer")
            .font(.system(size: 48))
            .foregroundStyle(KidViewTheme.accentYellow)
          Text("Time Limit Reached")
            .font(KidViewTheme.roundedFont(.title, weight: .black))
          Text("Ask a parent to unlock the app.")
            .font(KidViewTheme.roundedFont(.body))
            .foregroundStyle(KidViewTheme.secondaryText)
        }
        .foregroundStyle(KidViewTheme.primaryText)
        .padding(24)
        .background(KidViewTheme.surfaceNavy.opacity(0.96), in: RoundedRectangle(cornerRadius: 24))
        .overlay {
          RoundedRectangle(cornerRadius: 24)
            .stroke(KidViewTheme.dividerBlueGray.opacity(0.7), lineWidth: 1)
        }
      }
    }
    .navigationBarBackButtonHidden()
    .toolbar(.hidden, for: .navigationBar)
    .sheet(isPresented: $showingUnlock) {
      ParentUnlockView(store: store) {
        showingUnlock = false
        dismiss()
      }
      .presentationDetents([.medium])
    }
    .task {
      while !Task.isCancelled {
        now = Date()
        if timeLimitReached {
          showingUnlock = true
        }
        try? await Task.sleep(for: .seconds(1))
      }
    }
    .onAppear {
      startDate = Date()
      UIApplication.shared.isIdleTimerDisabled = true
    }
    .onDisappear {
      UIApplication.shared.isIdleTimerDisabled = false
    }
  }

  private var elapsedSeconds: Int {
    max(0, Int(now.timeIntervalSince(startDate)))
  }

  private var timeLimitStatus: TimeLimitStatus {
    TimeLimitPolicy.status(
      limitMinutes: store.settings.timeLimitMinutes, elapsedSeconds: elapsedSeconds)
  }

  private var timeLimitLabel: String? {
    TimeLimitPolicy.remainingLabel(remainingSeconds: timeLimitStatus.remainingSeconds).map {
      "\($0) left"
    }
  }

  private var timeLimitReached: Bool {
    timeLimitStatus.isReached
  }
}

struct ParentUnlockView: View {
  let store: ParentControlsStore
  let onUnlocked: () -> Void

  @State private var pin = ""
  @State private var errorMessage: String?

  var body: some View {
    AppBackground {
      KidViewCard(cornerRadius: 28, padding: 22) {
        VStack(spacing: 18) {
          Image(systemName: "lock.shield.fill")
            .font(.system(size: 44))
            .foregroundStyle(KidViewTheme.safetyGreen)

          Text("Child Mode is Active")
            .font(KidViewTheme.roundedFont(.title2, weight: .black))
            .foregroundStyle(KidViewTheme.primaryText)

          SecureField("Parent PIN", text: $pin)
            .keyboardType(.numberPad)
            .textContentType(.oneTimeCode)
            .textFieldStyle(.roundedBorder)
            .onChange(of: pin) { _, newValue in pin = ParentPinValidator.sanitize(newValue) }

          if let errorMessage {
            Text(errorMessage)
              .font(KidViewTheme.roundedFont(.callout, weight: .semibold))
              .foregroundStyle(KidViewTheme.error)
          }

          Button {
            if store.verifyParentPin(pin) {
              onUnlocked()
            } else {
              errorMessage = "Incorrect parent PIN."
              pin = ""
            }
          } label: {
            Label("Unlock", systemImage: "lock.open.fill")
              .frame(maxWidth: .infinity)
          }
          .buttonStyle(PrimaryKidViewButtonStyle())
        }
      }
      .padding(20)
    }
  }
}

struct ChangePinView: View {
  let store: ParentControlsStore

  @Environment(\.dismiss) private var dismiss
  @State private var currentPin = ""
  @State private var newPin = ""
  @State private var confirmPin = ""
  @State private var errorMessage: String?

  var body: some View {
    NavigationStack {
      AppBackground {
        VStack(spacing: 14) {
          KidViewCard(cornerRadius: 28, padding: 22) {
            Text("Change Parent PIN")
              .font(KidViewTheme.roundedFont(.title2, weight: .black))
              .foregroundStyle(KidViewTheme.primaryText)
            Text("Verify the current PIN first, then choose a new 4 to 8 digit PIN.")
              .font(KidViewTheme.roundedFont(.subheadline))
              .foregroundStyle(KidViewTheme.secondaryText)

            SecureField("Current PIN", text: $currentPin)
              .textFieldStyle(.roundedBorder)
            SecureField("New PIN", text: $newPin)
              .textFieldStyle(.roundedBorder)
            SecureField("Confirm new PIN", text: $confirmPin)
              .textFieldStyle(.roundedBorder)
          }

          if let errorMessage {
            KidViewCard {
              Label(errorMessage, systemImage: "exclamationmark.triangle.fill")
                .font(KidViewTheme.roundedFont(.callout, weight: .semibold))
                .foregroundStyle(KidViewTheme.error)
            }
          }

          Button {
            save()
          } label: {
            Label("Save PIN", systemImage: "checkmark.circle.fill")
              .frame(maxWidth: .infinity)
          }
          .buttonStyle(PrimaryKidViewButtonStyle())

          Spacer()
        }
        .padding(16)
      }
      .keyboardType(.numberPad)
      .navigationTitle("Parent PIN")
      .toolbarBackground(KidViewTheme.navy.opacity(0.96), for: .navigationBar)
      .toolbarColorScheme(.dark, for: .navigationBar)
      .toolbar {
        ToolbarItem(placement: .cancellationAction) {
          Button("Cancel") {
            dismiss()
          }
        }
      }
      .onChange(of: currentPin) { _, value in currentPin = ParentPinValidator.sanitize(value) }
      .onChange(of: newPin) { _, value in newPin = ParentPinValidator.sanitize(value) }
      .onChange(of: confirmPin) { _, value in confirmPin = ParentPinValidator.sanitize(value) }
    }
  }

  private func save() {
    if let validation = ParentPinValidator.validatePinChange(
      currentPin: currentPin,
      newPin: newPin,
      confirmPin: confirmPin
    ) {
      errorMessage = validation
      return
    }

    if store.updateParentPin(currentPin: currentPin, newPin: newPin) {
      dismiss()
    } else {
      errorMessage = "Current PIN is incorrect."
    }
  }
}

struct ParentShell<Content: View>: View {
  let store: ParentControlsStore
  let title: String
  let selectedTab: ParentTab
  @ViewBuilder let content: Content

  var body: some View {
    AppBackground {
      content
    }
    .navigationTitle(title)
    .toolbarBackground(KidViewTheme.navy.opacity(0.96), for: .navigationBar)
    .toolbarColorScheme(.dark, for: .navigationBar)
    .safeAreaInset(edge: .bottom) {
      HStack {
        tabButton(.home, title: "Home", icon: "house.fill")
        tabButton(.playlist, title: "Playlist", icon: "list.bullet.rectangle.fill")
        tabButton(.settings, title: "Settings", icon: "gearshape.fill")
      }
      .padding(.horizontal, 10)
      .padding(.top, 10)
      .padding(.bottom, 8)
      .background(KidViewTheme.surfaceNavy.opacity(0.96))
      .overlay(alignment: .top) {
        Rectangle()
          .fill(KidViewTheme.dividerBlueGray.opacity(0.7))
          .frame(height: 1)
      }
    }
  }

  private func tabButton(_ tab: ParentTab, title: String, icon: String) -> some View {
    Button {
      store.selectedTab = tab
    } label: {
      Label(title, systemImage: icon)
        .font(KidViewTheme.roundedFont(.caption, weight: selectedTab == tab ? .bold : .semibold))
        .frame(maxWidth: .infinity)
        .padding(.vertical, 8)
        .background(
          selectedTab == tab ? KidViewTheme.primaryBlue.opacity(0.28) : Color.clear,
          in: RoundedRectangle(cornerRadius: 14)
        )
    }
    .buttonStyle(.plain)
    .foregroundStyle(selectedTab == tab ? KidViewTheme.accentYellow : KidViewTheme.secondaryText)
  }
}

struct EmptyPlaylistView: View {
  var body: some View {
    VStack(spacing: 10) {
      Image(systemName: "play.slash")
        .font(.system(size: 42))
        .foregroundStyle(KidViewTheme.skyBlue)
      Text("No approved media yet")
        .font(KidViewTheme.roundedFont(.headline, weight: .bold))
        .foregroundStyle(KidViewTheme.primaryText)
      Text("Add a video or playlist to create a safe watch list.")
        .font(KidViewTheme.roundedFont(.subheadline))
        .foregroundStyle(KidViewTheme.secondaryText)
        .multilineTextAlignment(.center)
    }
    .frame(maxWidth: .infinity, maxHeight: .infinity)
    .padding(24)
    .background(KidViewTheme.surfaceNavy.opacity(0.92), in: RoundedRectangle(cornerRadius: 24))
    .overlay {
      RoundedRectangle(cornerRadius: 24)
        .stroke(KidViewTheme.dividerBlueGray.opacity(0.7), lineWidth: 1)
    }
  }
}

struct MediaCard: View {
  let item: ApprovedMediaItem
  let index: Int
  let selected: Bool
  let onSelect: () -> Void
  let onDelete: () -> Void

  var body: some View {
    Button(action: onSelect) {
      HStack(spacing: 12) {
        MediaGlyph(type: item.mediaType, index: index)
        VStack(alignment: .leading, spacing: 4) {
          HStack {
            Text(MediaDisplay.title(item, index: index))
              .font(KidViewTheme.roundedFont(.headline, weight: .bold))
              .foregroundStyle(KidViewTheme.primaryText)
            Spacer()
            StatusBadge(text: PlaybackPolicy.support(for: item).statusLabel)
          }
          Text(
            selected
              ? MediaDisplay.context(item, fallback: "Selected for child mode")
              : MediaDisplay.subtitle(item)
          )
          .font(KidViewTheme.roundedFont(.subheadline))
          .foregroundStyle(KidViewTheme.secondaryText)
          .lineLimit(2)
        }
        Button(role: .destructive, action: onDelete) {
          Image(systemName: "trash")
            .foregroundStyle(KidViewTheme.error)
        }
        .buttonStyle(.borderless)
      }
      .padding(14)
      .background(
        selected
          ? KidViewTheme.surfaceNavyAlt.opacity(0.94) : KidViewTheme.surfaceNavy.opacity(0.92),
        in: RoundedRectangle(cornerRadius: 22)
      )
      .overlay {
        RoundedRectangle(cornerRadius: 22)
          .stroke(
            selected ? KidViewTheme.accentYellow : KidViewTheme.dividerBlueGray.opacity(0.7),
            lineWidth: selected ? 2 : 1)
      }
    }
    .buttonStyle(.plain)
  }
}

struct MediaGlyph: View {
  let type: MediaType
  let index: Int

  var body: some View {
    ZStack {
      RoundedRectangle(cornerRadius: 16)
        .fill(
          type == .video
            ? KidViewTheme.primaryBlue.opacity(0.28) : KidViewTheme.accentYellow.opacity(0.22))
      Image(systemName: type == .video ? "play.fill" : "list.bullet")
        .font(.title3.weight(.bold))
        .foregroundStyle(type == .video ? KidViewTheme.skyBlue : KidViewTheme.accentYellow)
    }
    .frame(width: 52, height: 52)
    .overlay(alignment: .bottomTrailing) {
      Text("\(index + 1)")
        .font(KidViewTheme.roundedFont(.caption2, weight: .bold))
        .foregroundStyle(KidViewTheme.navyDeep)
        .padding(5)
        .background(KidViewTheme.accentYellow, in: Circle())
    }
  }
}

struct VideoSaveTargetRow: View {
  let title: String
  let subtitle: String
  let selected: Bool
  let enabled: Bool
  let action: () -> Void

  var body: some View {
    Button(action: action) {
      HStack(alignment: .top, spacing: 10) {
        Image(systemName: selected ? "checkmark.circle.fill" : "circle")
          .foregroundStyle(selected ? KidViewTheme.accentYellow : KidViewTheme.secondaryText)
          .padding(.top, 2)

        VStack(alignment: .leading, spacing: 3) {
          Text(title)
            .font(KidViewTheme.roundedFont(.subheadline, weight: .bold))
            .foregroundStyle(KidViewTheme.primaryText)
          Text(subtitle)
            .font(KidViewTheme.roundedFont(.caption))
            .foregroundStyle(KidViewTheme.secondaryText)
        }

        Spacer()
      }
      .padding(12)
      .background(
        selected
          ? KidViewTheme.surfaceNavyAlt.opacity(0.75) : KidViewTheme.surfaceNavyAlt.opacity(0.35),
        in: RoundedRectangle(cornerRadius: 16)
      )
      .overlay {
        RoundedRectangle(cornerRadius: 16)
          .stroke(
            selected
              ? KidViewTheme.accentYellow.opacity(0.7) : KidViewTheme.dividerBlueGray.opacity(0.4),
            lineWidth: 1
          )
      }
      .opacity(enabled ? 1 : 0.45)
    }
    .buttonStyle(.plain)
    .disabled(!enabled)
  }
}

struct AppBackground<Content: View>: View {
  @ViewBuilder let content: Content

  var body: some View {
    ZStack {
      KidViewTheme.backgroundGradient
        .ignoresSafeArea()

      Circle()
        .fill(KidViewTheme.primaryBlue.opacity(0.18))
        .frame(width: 220, height: 220)
        .offset(x: 125, y: -325)

      Circle()
        .fill(KidViewTheme.primaryBlue.opacity(0.24))
        .frame(width: 280, height: 280)
        .offset(x: -175, y: 315)

      Circle()
        .fill(KidViewTheme.safetyGreen.opacity(0.14))
        .frame(width: 190, height: 190)
        .offset(x: 155, y: 345)

      DecorativeStar()
        .offset(x: -150, y: -250)
      DecorativeStar(size: 12, opacity: 0.42)
        .offset(x: 135, y: -205)
      DecorativeStar(size: 9, opacity: 0.34)
        .offset(x: -165, y: -35)

      content
    }
  }
}

struct BrandWordmark: View {
  var compact = false

  var body: some View {
    HStack(spacing: 4) {
      Text("KidView")
        .foregroundStyle(KidViewTheme.primaryText)
      Text("Lock")
        .foregroundStyle(KidViewTheme.accentYellow)
    }
  }
}

struct BrandHeader: View {
  var subtitle = "Safe Videos. Happy Kids."

  var body: some View {
    HStack(spacing: 12) {
      Image("KidViewBrandIcon")
        .resizable()
        .scaledToFit()
        .frame(width: 54, height: 54)
        .clipShape(RoundedRectangle(cornerRadius: 16))
        .overlay {
          RoundedRectangle(cornerRadius: 16)
            .stroke(.white.opacity(0.12), lineWidth: 1)
        }

      VStack(alignment: .leading, spacing: 2) {
        BrandWordmark()
          .font(KidViewTheme.roundedFont(.title2, weight: .black))
        Text(subtitle)
          .font(KidViewTheme.roundedFont(.caption, weight: .semibold))
          .foregroundStyle(KidViewTheme.mutedText)
      }
    }
  }
}

struct DecorativeStar: View {
  var size: CGFloat = 15
  var opacity = 0.5

  var body: some View {
    Image(systemName: "sparkle")
      .font(.system(size: size, weight: .bold))
      .foregroundStyle(KidViewTheme.accentYellow.opacity(opacity))
  }
}

struct KidViewCard<Content: View>: View {
  var cornerRadius: CGFloat = 24
  var padding: CGFloat = 18
  @ViewBuilder let content: Content

  var body: some View {
    VStack(alignment: .leading, spacing: 12) {
      content
    }
    .padding(padding)
    .frame(maxWidth: .infinity, alignment: .leading)
    .background(
      KidViewTheme.surfaceNavy.opacity(0.92), in: RoundedRectangle(cornerRadius: cornerRadius)
    )
    .overlay {
      RoundedRectangle(cornerRadius: cornerRadius)
        .stroke(KidViewTheme.dividerBlueGray.opacity(0.65), lineWidth: 1)
    }
  }
}

struct StatusBadge: View {
  let text: String
  var color = KidViewTheme.safetyGreen

  var body: some View {
    Text(text)
      .font(KidViewTheme.roundedFont(.caption2, weight: .bold))
      .foregroundStyle(KidViewTheme.primaryText)
      .padding(.horizontal, 9)
      .padding(.vertical, 5)
      .background(color.opacity(0.22), in: Capsule())
      .overlay {
        Capsule()
          .stroke(color.opacity(0.45), lineWidth: 1)
      }
  }
}

struct PrimaryKidViewButtonStyle: ButtonStyle {
  func makeBody(configuration: Configuration) -> some View {
    configuration.label
      .font(KidViewTheme.roundedFont(.headline, weight: .bold))
      .foregroundStyle(KidViewTheme.navyDeep)
      .padding(.vertical, 15)
      .padding(.horizontal, 16)
      .background(
        KidViewTheme.accentYellow.opacity(configuration.isPressed ? 0.82 : 1),
        in: RoundedRectangle(cornerRadius: 18)
      )
      .scaleEffect(configuration.isPressed ? 0.98 : 1)
  }
}

struct SecondaryKidViewButtonStyle: ButtonStyle {
  func makeBody(configuration: Configuration) -> some View {
    configuration.label
      .font(KidViewTheme.roundedFont(.subheadline, weight: .bold))
      .foregroundStyle(KidViewTheme.primaryText)
      .padding(.vertical, 13)
      .padding(.horizontal, 14)
      .background(
        KidViewTheme.primaryBlue.opacity(configuration.isPressed ? 0.72 : 0.9),
        in: RoundedRectangle(cornerRadius: 16)
      )
      .overlay {
        RoundedRectangle(cornerRadius: 16)
          .stroke(KidViewTheme.skyBlue.opacity(0.25), lineWidth: 1)
      }
  }
}

struct YouTubePlayerView: UIViewRepresentable {
  let html: String

  func makeUIView(context: Context) -> WKWebView {
    let configuration = WKWebViewConfiguration()
    configuration.allowsInlineMediaPlayback = true
    configuration.mediaTypesRequiringUserActionForPlayback = []
    let webView = WKWebView(frame: .zero, configuration: configuration)
    webView.scrollView.isScrollEnabled = false
    webView.isOpaque = false
    webView.backgroundColor = .black
    return webView
  }

  func updateUIView(_ webView: WKWebView, context: Context) {
    webView.loadHTMLString(html, baseURL: URL(string: "https://www.youtube-nocookie.com"))
  }
}

struct ParsedYouTubeMedia {
  let mediaType: MediaType
  let youtubeId: String
}

enum YouTubeURLParser {
  static func parse(_ rawURL: String) -> ParsedYouTubeMedia? {
    let trimmed = rawURL.trimmingCharacters(in: .whitespacesAndNewlines)
    guard !trimmed.isEmpty,
      let components = URLComponents(string: trimmed),
      let host = components.host?.lowercased(),
      supportedHosts.contains(host)
    else {
      return nil
    }

    let query = Dictionary(
      uniqueKeysWithValues: (components.queryItems ?? []).compactMap { item in
        item.value.map { (item.name, $0) }
      })
    let pathSegments = components.path.split(separator: "/").map(String.init)

    if host == "youtu.be", let id = pathSegments.first, !id.isEmpty {
      return ParsedYouTubeMedia(mediaType: .video, youtubeId: id)
    }

    if components.path == "/watch" {
      if let videoId = query["v"], !videoId.isEmpty {
        return ParsedYouTubeMedia(mediaType: .video, youtubeId: videoId)
      }
      if let playlistId = query["list"], !playlistId.isEmpty {
        return ParsedYouTubeMedia(mediaType: .playlist, youtubeId: playlistId)
      }
    }

    if components.path == "/playlist", let playlistId = query["list"], !playlistId.isEmpty {
      return ParsedYouTubeMedia(mediaType: .playlist, youtubeId: playlistId)
    }

    if components.path == "/embed/videoseries", let playlistId = query["list"],
      !playlistId.isEmpty
    {
      return ParsedYouTubeMedia(mediaType: .playlist, youtubeId: playlistId)
    }

    if let first = pathSegments.first,
      ["shorts", "embed", "live"].contains(first),
      pathSegments.count > 1
    {
      return ParsedYouTubeMedia(mediaType: .video, youtubeId: pathSegments[1])
    }

    return nil
  }

  private static let supportedHosts = Set([
    "youtube.com", "www.youtube.com", "m.youtube.com", "music.youtube.com", "youtu.be",
    "youtube-nocookie.com", "www.youtube-nocookie.com",
  ])
}

enum YouTubeHTMLBuilder {
  static func html(for item: ApprovedMediaItem) -> String {
    switch item.mediaType {
    case .video:
      return playlistHTML(videoIds: [item.youtubeId])
    case .playlist:
      if item.playlistEntries.isEmpty {
        return hostedPlaylistHTML(playlistId: item.youtubeId)
      }
      return playlistHTML(videoIds: item.playlistEntries.map(\.youtubeId))
    }
  }

  private static func hostedPlaylistHTML(playlistId: String) -> String {
    let safePlaylistId = sanitize(playlistId)
    return """
      <!doctype html>
      <html><head><meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no">
      <style>html,body,iframe{margin:0;padding:0;width:100%;height:100%;background:#000;overflow:hidden;border:0;}</style></head>
      <body><iframe src="https://www.youtube-nocookie.com/embed/videoseries?list=\(safePlaylistId)&autoplay=1&playsinline=1&rel=0&fs=0&iv_load_policy=3&controls=1" allow="autoplay; encrypted-media" allowfullscreen></iframe></body></html>
      """
  }

  private static func playlistHTML(videoIds: [String]) -> String {
    let safeIds = Array(Set(videoIds.map { sanitize($0) }.filter { !$0.isEmpty }))
    let first = safeIds.first ?? ""
    let playlistArray = safeIds.map { "\"\($0)\"" }.joined(separator: ",")
    let loop = safeIds.count <= 1 && !first.isEmpty ? ", loop: 1, playlist: \"\(first)\"" : ""

    return """
      <!doctype html>
      <html><head><meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no">
      <style>html,body,#player{margin:0;padding:0;width:100%;height:100%;background:#000;overflow:hidden;border:0;}</style></head>
      <body><div id="player"></div>
      <script>
      var tag=document.createElement('script');tag.src="https://www.youtube.com/player_api";var firstScriptTag=document.getElementsByTagName('script')[0];firstScriptTag.parentNode.insertBefore(tag,firstScriptTag);
      var playlistIds=[\(playlistArray)];var player;
      function onYouTubePlayerAPIReady(){player=new YT.Player('player',{host:'https://www.youtube-nocookie.com',videoId:'\(first)',playerVars:{autoplay:1,playsinline:1,rel:0,fs:0,iv_load_policy:3,controls:1\(loop)},events:{onReady:function(event){if(playlistIds.length>1){event.target.loadPlaylist(playlistIds,0,0);}else{event.target.playVideo();}}}});}
      </script></body></html>
      """
  }

  private static func sanitize(_ raw: String) -> String {
    raw.filter { $0.isLetter || $0.isNumber || $0 == "_" || $0 == "-" }
  }
}

enum MediaDisplay {
  static func title(_ item: ApprovedMediaItem, index: Int? = nil) -> String {
    if !item.displayTitle.isEmpty {
      return item.displayTitle
    }
    switch item.mediaType {
    case .video:
      return index.map { "Approved Video \($0 + 1)" } ?? "Approved Video"
    case .playlist:
      return index.map { "Approved Playlist \($0 + 1)" } ?? "Approved Playlist"
    }
  }

  static func subtitle(_ item: ApprovedMediaItem, playbackStatus: String? = nil) -> String {
    if !item.displaySubtitle.isEmpty {
      return item.displaySubtitle
    }
    if item.mediaType == .playlist, !item.playlistEntries.isEmpty {
      return item.playlistEntries.count == 1 ? "1 video" : "\(item.playlistEntries.count) videos"
    }
    if let expectedItemCount = item.expectedItemCount, expectedItemCount > 0 {
      return expectedItemCount == 1 ? "1 video" : "\(expectedItemCount) videos"
    }
    if let playbackStatus, !playbackStatus.isEmpty {
      return playbackStatus
    }
    return item.mediaType == .video ? "Saved video link" : "Saved playlist link"
  }

  static func context(_ item: ApprovedMediaItem, fallback: String = "") -> String {
    if !item.contextNote.isEmpty {
      return item.contextNote
    }
    if item.mediaType == .playlist,
      let firstTitle = item.playlistEntries.first?.displayTitle,
      !firstTitle.isEmpty
    {
      return "Starts with \(firstTitle)"
    }
    return fallback
  }
}

struct PlaybackSupport {
  let isPlayableNow: Bool
  let statusLabel: String
  let detailMessage: String
}

enum PlaybackPolicy {
  static func support(for item: ApprovedMediaItem?) -> PlaybackSupport {
    guard let item else {
      return PlaybackSupport(
        isPlayableNow: false, statusLabel: "Unavailable",
        detailMessage: "The selected approved item is no longer available.")
    }

    switch item.mediaType {
    case .video:
      return PlaybackSupport(
        isPlayableNow: true, statusLabel: "Playable now",
        detailMessage: "This approved video can launch in child mode right now.")
    case .playlist:
      if !item.playlistEntries.isEmpty {
        return PlaybackSupport(
          isPlayableNow: true, statusLabel: "Playlist ready",
          detailMessage: "This curated playlist can launch in child mode right now.")
      }
      if !item.youtubeId.isEmpty {
        return PlaybackSupport(
          isPlayableNow: true, statusLabel: "Playlist link ready",
          detailMessage: "This YouTube playlist link can launch in child mode right now.")
      }
      return PlaybackSupport(
        isPlayableNow: false, statusLabel: "Unavailable",
        detailMessage:
          "Add a YouTube playlist link or at least one playlist video before starting child mode.")
    }
  }
}

enum ParentPinValidator {
  static func sanitize(_ value: String) -> String {
    String(value.filter(\.isNumber).prefix(8))
  }

  static func validateNewPin(pin: String, confirmPin: String) -> String? {
    if pin.count < 4 {
      return "Use at least 4 digits for the parent PIN."
    }
    if pin != confirmPin {
      return "PIN entries do not match."
    }
    return nil
  }

  static func validatePinChange(currentPin: String, newPin: String, confirmPin: String) -> String? {
    if currentPin.isEmpty {
      return "Enter the current parent PIN."
    }
    if currentPin == newPin {
      return "Choose a new PIN that is different from the current PIN."
    }
    return validateNewPin(pin: newPin, confirmPin: confirmPin)
  }
}

struct StoredPinHash: Codable {
  let saltBase64: String
  let hashBase64: String
}

enum PinHasher {
  static func create(pin: String) -> StoredPinHash {
    var salt = Data(count: 16)
    let result = salt.withUnsafeMutableBytes { buffer in
      guard let baseAddress = buffer.baseAddress else {
        return errSecAllocate
      }
      return SecRandomCopyBytes(kSecRandomDefault, buffer.count, baseAddress)
    }

    if result != errSecSuccess {
      salt = Data(UUID().uuidString.utf8)
    }

    return StoredPinHash(
      saltBase64: salt.base64EncodedString(),
      hashBase64: hash(pin: pin, salt: salt).base64EncodedString()
    )
  }

  static func verify(pin: String, stored: StoredPinHash) -> Bool {
    guard let salt = Data(base64Encoded: stored.saltBase64),
      let expected = Data(base64Encoded: stored.hashBase64)
    else {
      return false
    }
    return hash(pin: pin, salt: salt) == expected
  }

  private static func hash(pin: String, salt: Data) -> Data {
    var input = Data()
    input.append(salt)
    input.append(Data(pin.utf8))
    return Data(SHA256.hash(data: input))
  }
}

struct TimeLimitStatus {
  let remainingSeconds: Int?
  let isReached: Bool
}

enum TimeLimitPolicy {
  static func status(limitMinutes: Int?, elapsedSeconds: Int) -> TimeLimitStatus {
    guard let limitMinutes, limitMinutes > 0 else {
      return TimeLimitStatus(remainingSeconds: nil, isReached: false)
    }
    let totalSeconds = limitMinutes * 60
    return TimeLimitStatus(
      remainingSeconds: max(0, totalSeconds - elapsedSeconds),
      isReached: elapsedSeconds >= totalSeconds
    )
  }

  static func remainingLabel(remainingSeconds: Int?) -> String? {
    guard let remainingSeconds else { return nil }
    return String(format: "%02d:%02d", remainingSeconds / 60, remainingSeconds % 60)
  }
}

enum TimeLimitFormatter {
  static func summary(minutes: Int?) -> String {
    minutes.map { "\($0) minutes" } ?? "Off"
  }

  static func timerFace(minutes: Int?) -> String {
    minutes.map { String(format: "%02d:00", $0) } ?? "--:--"
  }

  static func endAtLabel(minutes: Int?) -> String {
    guard let minutes else { return "No end time set" }
    let date = Date().addingTimeInterval(TimeInterval(minutes * 60))
    let formatter = DateFormatter()
    formatter.timeStyle = .short
    return "Ends at \(formatter.string(from: date))"
  }
}

#Preview {
  ContentView()
}
