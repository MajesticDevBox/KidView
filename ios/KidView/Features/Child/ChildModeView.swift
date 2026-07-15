import SwiftUI

struct ChildModeView: View {
    let media: ApprovedMediaItem

    @EnvironmentObject private var session: AppSession
    @EnvironmentObject private var guidedAccessMonitor: GuidedAccessMonitor

    var body: some View {
        VStack(spacing: 0) {
            HStack {
                VStack(alignment: .leading, spacing: 4) {
                    Text(media.fallbackTitle)
                        .font(.headline)
                        .lineLimit(1)
                    Text(guidedAccessMonitor.isGuidedAccessEnabled ? "Guided Access active" : "Enable Guided Access for stronger lock-in")
                        .font(.caption)
                        .foregroundStyle(.secondary)
                }

                Spacer()

                Button("End") {
                    session.stopChildMode()
                }
                .buttonStyle(.bordered)
            }
            .padding()
            .background(.thinMaterial)

            YouTubePlayerView(media: media)
                .ignoresSafeArea(edges: .bottom)
        }
        .background(Color.black.ignoresSafeArea())
    }
}

#Preview {
    ChildModeView(media: PreviewData.sampleMedia[0])
        .environmentObject(AppSession())
        .environmentObject(GuidedAccessMonitor())
}
