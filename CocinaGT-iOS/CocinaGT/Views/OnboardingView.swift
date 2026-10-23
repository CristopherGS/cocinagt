import SwiftUI

/// Onboarding: captura presupuesto, tamaño del hogar y preferencias.
struct OnboardingView: View {
    @EnvironmentObject var model: AppViewModel
    @State private var step = 0

    var body: some View {
        VStack(spacing: 0) {
            header

            TabView(selection: $step) {
                welcomeStep.tag(0)
                budgetStep.tag(1)
                householdStep.tag(2)
                mealsStep.tag(3)
            }
            .tabViewStyle(.page(indexDisplayMode: .always))
            .animation(.easeInOut, value: step)

            footer
        }
        .background(Color(.systemGroupedBackground))
    }

    private var header: some View {
        VStack(spacing: 4) {
            Text("🍳 CocinaGT")
                .font(.largeTitle).bold()
                .foregroundStyle(Color.cocinaGreen)
            Text("Planes de comida económicos para Guatemala")
                .font(.subheadline)
                .foregroundStyle(.secondary)
        }
        .padding(.top, 24)
    }

    private var welcomeStep: some View {
        VStack(spacing: 20) {
            Spacer()
            Text("¡Bienvenido!")
                .font(.title).bold()
            Text("Te armamos un menú semanal con platillos chapines que se ajusta a tu presupuesto en Quetzales. Dinos un par de cosas para empezar.")
                .multilineTextAlignment(.center)
                .foregroundStyle(.secondary)
                .padding(.horizontal, 32)
            Spacer()
        }
    }

    private var budgetStep: some View {
        VStack(spacing: 24) {
            Spacer()
            Text("¿Cuánto quieres gastar por semana?")
                .font(.title2).bold()
                .multilineTextAlignment(.center)
            Text(Money.gtq(model.prefs.weeklyBudget))
                .font(.system(size: 44, weight: .heavy, design: .rounded))
                .foregroundStyle(Color.cocinaGreen)
            Slider(value: $model.prefs.weeklyBudget, in: 150...1200, step: 25)
                .padding(.horizontal, 32)
            Text("Puedes cambiarlo después en Ajustes.")
                .font(.footnote).foregroundStyle(.secondary)
            Spacer()
        }
    }

    private var householdStep: some View {
        VStack(spacing: 24) {
            Spacer()
            Text("¿Para cuántas personas cocinas?")
                .font(.title2).bold()
                .multilineTextAlignment(.center)
            Stepper(value: $model.prefs.householdSize, in: 1...12) {
                Text("\(model.prefs.householdSize) persona\(model.prefs.householdSize == 1 ? "" : "s")")
                    .font(.title3).bold()
            }
            .padding(.horizontal, 48)
            Spacer()
        }
    }

    private var mealsStep: some View {
        VStack(spacing: 20) {
            Spacer()
            Text("¿Qué comidas planificamos?")
                .font(.title2).bold()
            ForEach(UserPreferences.mealTypeOptions, id: \.self) { type in
                Toggle(type, isOn: Binding(
                    get: { model.prefs.mealTypes.contains(type) },
                    set: { on in
                        if on { model.prefs.mealTypes.insert(type) }
                        else { model.prefs.mealTypes.remove(type) }
                    }
                ))
                .padding(.horizontal, 48)
            }
            Toggle("Solo recetas vegetarianas", isOn: $model.prefs.vegetarianOnly)
                .padding(.horizontal, 48)
                .padding(.top, 8)
            Spacer()
        }
    }

    private var footer: some View {
        Button {
            if step < 3 {
                withAnimation { step += 1 }
            } else {
                model.completeOnboarding()
            }
        } label: {
            Text(step < 3 ? "Siguiente" : "Crear mi plan")
                .font(.headline)
                .frame(maxWidth: .infinity)
                .padding()
                .background(Color.cocinaGreen)
                .foregroundStyle(.white)
                .clipShape(RoundedRectangle(cornerRadius: 14))
        }
        .disabled(step == 3 && model.prefs.mealTypes.isEmpty)
        .padding()
    }
}

#Preview {
    OnboardingView().environmentObject(AppViewModel())
}
