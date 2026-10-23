import Foundation

/// Genera un plan semanal de comidas que intenta ajustarse al presupuesto del hogar.
struct MealPlanGenerator {

    let recipes: [Recipe]

    init(recipes: [Recipe] = RecipeStore.shared.recipes) {
        self.recipes = recipes
    }

    /// Crea un plan semanal para las preferencias dadas.
    ///
    /// Estrategia:
    /// 1. Filtra recetas por tipo de comida y dieta.
    /// 2. Asigna un presupuesto por comida (presupuesto semanal / número de comidas).
    /// 3. Para cada día y tipo de comida, elige una receta priorizando las que caben
    ///    en el presupuesto por comida, rotando para no repetir demasiado.
    func makePlan(for prefs: UserPreferences) -> WeeklyPlan {
        let days = 7
        let mealTypes = UserPreferences.mealTypeOptions.filter { prefs.mealTypes.contains($0) }
        let totalMeals = max(days * mealTypes.count, 1)
        let perMealBudget = prefs.weeklyBudget / Double(totalMeals)

        var meals: [PlannedMeal] = []
        // Control de repeticiones por tipo de comida.
        var rotationIndex: [String: Int] = [:]

        for day in 0..<days {
            for type in mealTypes {
                let candidates = pool(for: type, prefs: prefs)
                guard !candidates.isEmpty else { continue }

                let chosen = choose(from: candidates,
                                    perMealBudget: perMealBudget,
                                    householdSize: prefs.householdSize,
                                    rotation: &rotationIndex,
                                    mealType: type)

                let cost = costForHousehold(recipe: chosen, householdSize: prefs.householdSize)
                meals.append(PlannedMeal(dayIndex: day,
                                         mealType: type,
                                         recipeId: chosen.id,
                                         servings: prefs.householdSize,
                                         cost: cost))
            }
        }

        return WeeklyPlan(meals: meals, budget: prefs.weeklyBudget)
    }

    /// Recetas candidatas para un tipo de comida, aplicando filtro vegetariano.
    private func pool(for mealType: String, prefs: UserPreferences) -> [Recipe] {
        var pool = recipes.filter { $0.category == mealType }
        // Para la cena también permitimos snacks ligeros si no hay suficientes opciones.
        if mealType == "Cena" && pool.count < 3 {
            pool += recipes.filter { $0.category == "Snack" }
        }
        if prefs.vegetarianOnly {
            pool = pool.filter { $0.tags.contains("vegetariano") }
        }
        return pool.sorted { $0.costPerServing < $1.costPerServing }
    }

    /// Elige una receta: primero intenta respetar el presupuesto por comida; si ninguna
    /// cabe, elige la más barata disponible. Rota para variar los menús.
    private func choose(from candidates: [Recipe],
                        perMealBudget: Double,
                        householdSize: Int,
                        rotation: inout [String: Int],
                        mealType: String) -> Recipe {
        let affordable = candidates.filter {
            costForHousehold(recipe: $0, householdSize: householdSize) <= perMealBudget
        }
        let working = affordable.isEmpty ? candidates : affordable

        let index = (rotation[mealType] ?? 0) % working.count
        rotation[mealType] = index + 1
        return working[index]
    }

    /// Costo de cocinar una receta para todo el hogar (porción × personas), en GTQ.
    func costForHousehold(recipe: Recipe, householdSize: Int) -> Double {
        (recipe.costPerServing * Double(householdSize) * 100).rounded() / 100
    }
}
