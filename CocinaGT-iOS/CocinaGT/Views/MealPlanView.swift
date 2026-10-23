import SwiftUI

/// Muestra el plan semanal día por día con el resumen de presupuesto.
struct MealPlanView: View {
    @EnvironmentObject var model: AppViewModel

    var body: some View {
        NavigationStack {
            Group {
                if let plan = model.plan, !plan.meals.isEmpty {
                    List {
                        budgetSummary(plan)
                        ForEach(0..<7, id: \.self) { day in
                            let meals = plan.meals(forDay: day)
                            if !meals.isEmpty {
                                Section(WeeklyPlan.dayNames[day]) {
                                    ForEach(meals) { meal in
                                        mealRow(meal)
                                    }
                                }
                            }
                        }
                    }
                } else {
                    ContentUnavailableView("Aún no hay plan",
                                           systemImage: "calendar.badge.plus",
                                           description: Text("Genera tu primer plan semanal."))
                }
            }
            .navigationTitle("Mi plan semanal")
            .toolbar {
                ToolbarItem(placement: .topBarTrailing) {
                    Button {
                        model.regeneratePlan()
                    } label: {
                        Label("Regenerar", systemImage: "arrow.clockwise")
                    }
                }
            }
        }
    }

    private func budgetSummary(_ plan: WeeklyPlan) -> some View {
        Section {
            VStack(alignment: .leading, spacing: 10) {
                HStack {
                    Text("Presupuesto")
                    Spacer()
                    Text(Money.gtq(plan.budget)).bold()
                }
                HStack {
                    Text("Costo estimado")
                    Spacer()
                    Text(Money.gtq(plan.totalCost)).bold()
                        .foregroundStyle(plan.isWithinBudget ? Color.cocinaGreen : .red)
                }
                ProgressView(value: min(plan.totalCost, plan.budget), total: plan.budget)
                    .tint(plan.isWithinBudget ? .cocinaGreen : .red)
                Text(plan.isWithinBudget
                     ? "Te quedan \(Money.gtq(plan.remaining)) de margen 🎉"
                     : "Te pasas por \(Money.gtq(-plan.remaining)). Prueba regenerar o baja comidas.")
                    .font(.footnote)
                    .foregroundStyle(.secondary)
            }
            .padding(.vertical, 4)
        }
    }

    private func mealRow(_ meal: PlannedMeal) -> some View {
        Group {
            if let recipe = model.recipe(for: meal) {
                NavigationLink {
                    RecipeDetailView(recipe: recipe, servings: meal.servings)
                } label: {
                    HStack {
                        VStack(alignment: .leading, spacing: 2) {
                            Text(meal.mealType)
                                .font(.caption).foregroundStyle(.secondary)
                            Text(recipe.name).font(.body)
                        }
                        Spacer()
                        Text(Money.gtq(meal.cost))
                            .font(.subheadline).bold()
                            .foregroundStyle(Color.cocinaGreen)
                    }
                }
            }
        }
    }
}

#Preview {
    let m = AppViewModel()
    m.regeneratePlan()
    return MealPlanView().environmentObject(m)
}
