import Foundation

/// Preferencias del hogar que alimentan el generador de planes.
struct UserPreferences: Codable, Equatable {
    /// Presupuesto semanal disponible para comida, en GTQ.
    var weeklyBudget: Double = 350
    /// Número de personas en el hogar.
    var householdSize: Int = 4
    /// Tipos de comida que se desean planificar.
    var mealTypes: Set<String> = ["Desayuno", "Almuerzo", "Cena"]
    /// Solo recetas vegetarianas.
    var vegetarianOnly: Bool = false
    /// Indica si el usuario ya completó el onboarding.
    var hasOnboarded: Bool = false

    static let mealTypeOptions = ["Desayuno", "Almuerzo", "Cena"]
}
