import Foundation

/// Una comida planificada para un día y tipo específico.
struct PlannedMeal: Codable, Hashable, Identifiable {
    var id = UUID()
    let dayIndex: Int          // 0 = Lunes ... 6 = Domingo
    let mealType: String       // Desayuno / Almuerzo / Cena
    let recipeId: String
    /// Porciones a cocinar (normalmente = tamaño del hogar).
    let servings: Int
    /// Costo estimado de esta comida para el hogar, en GTQ.
    let cost: Double
}

/// Plan semanal completo generado para el hogar.
struct WeeklyPlan: Codable, Hashable {
    var meals: [PlannedMeal]
    var budget: Double

    static let dayNames = ["Lunes", "Martes", "Miércoles", "Jueves", "Viernes", "Sábado", "Domingo"]

    /// Costo total estimado de todo el plan, en GTQ.
    var totalCost: Double {
        meals.reduce(0) { $0 + $1.cost }
    }

    /// Cuánto se mantiene por debajo (o encima) del presupuesto.
    var remaining: Double {
        budget - totalCost
    }

    var isWithinBudget: Bool {
        totalCost <= budget
    }

    func meals(forDay day: Int) -> [PlannedMeal] {
        meals.filter { $0.dayIndex == day }
            .sorted { UserPreferences.mealTypeOptions.firstIndex(of: $0.mealType) ?? 0
                    < UserPreferences.mealTypeOptions.firstIndex(of: $1.mealType) ?? 0 }
    }
}
