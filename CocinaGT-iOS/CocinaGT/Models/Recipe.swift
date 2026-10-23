import Foundation

/// Un ingrediente dentro de una receta, con su costo estimado en Quetzales (GTQ).
struct Ingredient: Codable, Hashable, Identifiable {
    var id: String { "\(name)-\(unit)" }
    let name: String
    let quantity: Double
    let unit: String
    let group: String
    /// Costo estimado de la cantidad indicada, en GTQ.
    let cost: Double
}

/// Una receta guatemalteca con su información de costo y preparación.
struct Recipe: Codable, Hashable, Identifiable {
    let id: String
    let name: String
    let category: String
    let servings: Int
    let prepMinutes: Int
    /// Costo aproximado por porción, en GTQ.
    let costPerServing: Double
    let tags: [String]
    let description: String
    let ingredients: [Ingredient]
    let steps: [String]

    /// Costo total de preparar la receta completa (todas sus porciones).
    var totalCost: Double {
        ingredients.reduce(0) { $0 + $1.cost }
    }
}

/// Contenedor del archivo recipes.json.
struct RecipeDatabase: Codable {
    let currency: String
    let currencySymbol: String
    let country: String
    let categories: [String]
    let ingredientGroups: [String]
    let recipes: [Recipe]
}
