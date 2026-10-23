import SwiftUI

@main
struct CocinaGTApp: App {
    @StateObject private var model = AppViewModel()

    var body: some Scene {
        WindowGroup {
            RootView()
                .environmentObject(model)
                .tint(.cocinaGreen)
        }
    }
}

extension Color {
    /// Verde chapín de la marca CocinaGT.
    static let cocinaGreen = Color(red: 0.0, green: 0.52, blue: 0.36)
    static let cocinaAccent = Color(red: 0.96, green: 0.62, blue: 0.11)
}
