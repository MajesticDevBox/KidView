import SwiftUI

struct ParentHomeView: View {
    @EnvironmentObject private var session: AppSession
    @EnvironmentObject private var approvedMediaStore: ApprovedMediaStore
    @EnvironmentObject private var guidedAccessMonitor: GuidedAccessMonitor

    @State private var isPresentingAddMedia = false

    var body: some View {
        NavigationStack {
            List {
                Section("Child Mode") {
                    if let selectedItem = approvedMediaStore.selectedItem {
                        VStack(alignment: .leading, spacing: 8) {
                            Text(selectedItem.fallbackTitle)
                                .font(.headline)
                            Text("Selected \(selectedItem.mediaType.rawValue)")
                                .foregroundStyle(.secondary)

                            Button("Start Child Mode") {
                                session.startChildMode(with: selectedItem)
                            }
                            .buttonStyle(.borderedProminent)
                        }
                    } else {
                        Text("Add an approved video or playlist before starting child mode.")
                            .foregroundStyle(.secondary)
                    }

                    Label(
                        guidedAccessMonitor.isGuidedAccessEnabled
                        ? "Guided Access is active"
                        : "Guided Access is not active",
                        systemImage: guidedAccessMonitor.isGuidedAccessEnabled ? "lock.fill" : "lock.open"
                    )
                    .font(.subheadline)
                    .foregroundStyle(guidedAccessMonitor.isGuidedAccessEnabled ? .green : .orange)
                }

                Section("Approved Media") {
                    if approvedMediaStore.items.isEmpty {
                        Text("No approved media yet.")
                            .foregroundStyle(.secondary)
                    } else {
                        ForEach(approvedMediaStore.items) { item in
                            Button {
                                approvedMediaStore.select(item)
                            } label: {
                                HStack {
                                    VStack(alignment: .leading, spacing: 4) {
                                        Text(item.fallbackTitle)
                                            .foregroundStyle(.primary)
                                        Text(item.youtubeID)
                                            .font(.caption)
                                            .foregroundStyle(.secondary)
                                    }

                                    Spacer()

                                    if approvedMediaStore.selectedItemID == item.id {
                                        Image(systemName: "checkmark.circle.fill")
                                            .foregroundStyle(.accent)
                                    }
                                }
                            }
                            .buttonStyle(.plain)
                        }
                        .onDelete(perform: approvedMediaStore.delete)
                    }
                }
            }
            .navigationTitle("KidView Parent")
            .toolbar {
                ToolbarItem(placement: .topBarTrailing) {
                    Button {
                        isPresentingAddMedia = true
                    } label: {
                        Label("Add", systemImage: "plus")
                    }
                }
            }
            .sheet(isPresented: $isPresentingAddMedia) {
                AddMediaView()
                    .environmentObject(approvedMediaStore)
            }
        }
    }
}

#Preview {
    ParentHomeView()
        .environmentObject(AppSession())
        .environmentObject(ApprovedMediaStore(previewItems: PreviewData.sampleMedia))
        .environmentObject(GuidedAccessMonitor())
}
