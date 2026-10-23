import SwiftUI

/// Ajustes: permite cambiar presupuesto, hogar y preferencias, y regenerar el plan.
struct SettingsView: View {
    @EnvironmentObject var model: AppViewModel

    var body: some View {
        NavigationStack {
            Form {
                Section("Presupuesto semanal") {
                    HStack {
                        Text(Money.gtq(model.prefs.weeklyBudget))
                            .font(.title3).bold()
                            .foregroundStyle(Color.cocinaGreen)
                        Spacer()
                    }
                    Slider(value: $model.prefs.weeklyBudget, in: 150...1200, step: 25)
                }

                Section("Hogar") {
                    Stepper(value: $model.prefs.householdSize, in: 1...12) {
                        Text("\(model.prefs.householdSize) persona\(model.prefs.householdSize == 1 ? "" : "s")")
                    }
                }

                Section("Comidas a planificar") {
                    ForEach(UserPreferences.mealTypeOptions, id: \.self) { type in
                        Toggle(type, isOn: Binding(
                            get: { model.prefs.mealTypes.contains(type) },
                            set: { on in
                                if on { model.prefs.mealTypes.insert(type) }
                                else { model.prefs.mealTypes.remove(type) }
                            }
                        ))
                    }
                    Toggle("Solo vegetarianas", isOn: $model.prefs.vegetarianOnly)
                }

                Section {
                    Button {
                        model.regeneratePlan()
                    } label: {
                        Label("Regenerar plan semanal", systemImage: "arrow.clockwise")
                    }
                    .disabled(model.prefs.mealTypes.isEmpty)
                }

                Section {
                    Text("CocinaGT · Planes de comida económicos para Guatemala")
                        .font(.footnote).foregroundStyle(.secondary)
                    Text("Precios en Quetzales (GTQ). Son estimados de mercado y pueden variar.")
                        .font(.footnote).foregroundStyle(.secondary)
                }
            }
            .navigationTitle("Ajustes")
        }
    }
}

#Preview {
    SettingsView().environmentObject(AppViewModel())
}
