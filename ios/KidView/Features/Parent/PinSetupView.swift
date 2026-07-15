import SwiftUI

struct PinSetupView: View {
    @EnvironmentObject private var parentPinStore: ParentPinStore

    @State private var pin = ""
    @State private var confirmPin = ""
    @State private var errorMessage = ""

    var body: some View {
        NavigationStack {
            Form {
                Section("Create Parent PIN") {
                    SecureField("4+ digit PIN", text: $pin)
                        .keyboardType(.numberPad)
                    SecureField("Confirm PIN", text: $confirmPin)
                        .keyboardType(.numberPad)
                }

                if !errorMessage.isEmpty {
                    Section {
                        Text(errorMessage)
                            .foregroundStyle(.red)
                    }
                }

                Section {
                    Button("Save PIN", action: savePin)
                        .buttonStyle(.borderedProminent)
                        .frame(maxWidth: .infinity, alignment: .center)
                }
            }
            .navigationTitle("KidView Setup")
        }
    }

    private func savePin() {
        errorMessage = ""

        guard pin == confirmPin else {
            errorMessage = "PIN entries must match."
            return
        }

        guard parentPinStore.save(pin: pin) else {
            errorMessage = "Use at least 4 digits for the parent PIN."
            return
        }

        pin = ""
        confirmPin = ""
    }
}

#Preview {
    PinSetupView()
        .environmentObject(ParentPinStore())
}
