import SwiftUI

struct AddMediaView: View {
    @Environment(\.dismiss) private var dismiss
    @EnvironmentObject private var approvedMediaStore: ApprovedMediaStore

    @State private var rawURL = ""
    @State private var title = ""
    @State private var note = ""
    @State private var errorMessage = ""

    private var parsedPreview: ParsedYouTubeMedia? {
        YouTubeURLParser.parse(rawURL)
    }

    var body: some View {
        NavigationStack {
            Form {
                Section("YouTube Link") {
                    TextField("Paste video or playlist URL", text: $rawURL, axis: .vertical)
                        .textInputAutocapitalization(.never)
                        .autocorrectionDisabled()
                        .keyboardType(.URL)
                }

                Section("Optional Labels") {
                    TextField("Display title", text: $title)
                    TextField("Parent note", text: $note, axis: .vertical)
                }

                Section("Parser Preview") {
                    if let parsedPreview {
                        LabeledContent("Type") {
                            Text(parsedPreview.mediaType.rawValue.capitalized)
                        }

                        LabeledContent("YouTube ID") {
                            Text(parsedPreview.youtubeID)
                        }
                    } else {
                        Text("Paste a supported YouTube URL to preview the parsed ID.")
                            .foregroundStyle(.secondary)
                    }
                }

                if !errorMessage.isEmpty {
                    Section {
                        Text(errorMessage)
                            .foregroundStyle(.red)
                    }
                }
            }
            .navigationTitle("Add Approved Media")
            .toolbar {
                ToolbarItem(placement: .topBarLeading) {
                    Button("Cancel") { dismiss() }
                }

                ToolbarItem(placement: .topBarTrailing) {
                    Button("Save", action: save)
                        .disabled(parsedPreview == nil)
                }
            }
        }
    }

    private func save() {
        let success = approvedMediaStore.addApprovedMedia(
            from: rawURL,
            title: title.trimmingCharacters(in: .whitespacesAndNewlines),
            note: note.trimmingCharacters(in: .whitespacesAndNewlines)
        )

        if success {
            dismiss()
        } else {
            errorMessage = "That URL does not look like a supported YouTube video or playlist."
        }
    }
}

#Preview {
    AddMediaView()
        .environmentObject(ApprovedMediaStore(previewItems: PreviewData.sampleMedia))
}
