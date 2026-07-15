import SwiftUI

struct RootView: View {
    @EnvironmentObject private var session: AppSession
    @EnvironmentObject private var approvedMediaStore: ApprovedMediaStore
    @EnvironmentObject private var parentPinStore: ParentPinStore
    @EnvironmentObject private var guidedAccessMonitor: GuidedAccessMonitor

    var body: some View {
        Group {
            if parentPinStore.hasPin {
                ParentHomeView()
            } else {
                PinSetupView()
            }
        }
        .fullScreenCover(isPresented: $session.isChildModePresented) {
            if let media = session.activeMedia {
                ChildModeView(media: media)
                    .environmentObject(session)
                    .environmentObject(approvedMediaStore)
                    .environmentObject(parentPinStore)
                    .environmentObject(guidedAccessMonitor)
            }
        }
    }
}

#Preview {
    RootView()
        .environmentObject(AppSession())
        .environmentObject(ApprovedMediaStore(previewItems: PreviewData.sampleMedia))
        .environmentObject(ParentPinStore(hasPinOverride: true))
        .environmentObject(GuidedAccessMonitor())
}
