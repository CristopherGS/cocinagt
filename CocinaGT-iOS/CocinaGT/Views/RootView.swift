import SwiftUI

/// Decide si mostrar el onboarding o la app principal.
struct RootView: View {
    @EnvironmentObject var model: AppViewModel

    var body: some View {
        if model.prefs.hasOnboarded {
            MainTabView()
        } else {
            OnboardingView()
        }
    }
}

struct MainTabView: View {
    var body: some View {
        TabView {
            MealPlanView()
                .tabItem { Label("Mi plan", systemImage: "calendar") }

            ShoppingListView()
                .tabItem { Label("Compras", systemImage: "cart") }

            RecipesView()
                .tabItem { Label("Recetas", systemImage: "fork.knife") }

            SettingsView()
                .tabItem { Label("Ajustes", systemImage: "gearshape") }
        }
    }
}

#Preview {
    RootView().environmentObject(AppViewModel())
}
