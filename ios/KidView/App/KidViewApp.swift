import SwiftUI

@main
struct KidViewApp: App {
    @StateObject private var session = AppSession()
    @StateObject private var approvedMediaStore = ApprovedMediaStore()
    @StateObject private var parentPinStore = ParentPinStore()
    @StateObject private var guidedAccessMonitor = GuidedAccessMonitor()

    var body: some Scene {
        WindowGroup {
            RootView()
                .environmentObject(session)
                .environmentObject(approvedMediaStore)
                .environmentObject(parentPinStore)
                .environmentObject(guidedAccessMonitor)
        }
    }
}
