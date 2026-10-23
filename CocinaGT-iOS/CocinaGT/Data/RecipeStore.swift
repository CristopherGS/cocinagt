import Foundation

/// Carga y expone el catálogo de recetas desde recipes.json en el bundle.
final class RecipeStore {
    static let shared = RecipeStore()

    let database: RecipeDatabase
    var recipes: [Recipe] { database.recipes }

    /// Mapa de nombre de ingrediente -> término de búsqueda para la API de tiendas.
    let ingredientQueries: [String: String]

    private init() {
        if let url = Bundle.main.url(forResource: "recipes", withExtension: "json"),
           let data = try? Data(contentsOf: url),
           let decoded = try? JSONDecoder().decode(RecipeDatabase.self, from: data) {
            self.database = decoded
        } else {
            // Base vacía como respaldo; la app sigue funcionando sin crashear.
            self.database = RecipeDatabase(currency: "GTQ", currencySymbol: "Q",
                                           country: "Guatemala", categories: [],
                                           ingredientGroups: [], recipes: [])
        }

        self.ingredientQueries = Self.loadQueries()
    }

    private static func loadQueries() -> [String: String] {
        guard let url = Bundle.main.url(forResource: "ingredient_queries", withExtension: "json"),
              let data = try? Data(contentsOf: url),
              let root = try? JSONDecoder().decode([String: AnyQueryMap].self, from: data),
              let map = root["queries"]?.value else {
            return [:]
        }
        return map
    }

    func recipe(withId id: String) -> Recipe? {
        recipes.first { $0.id == id }
    }

    /// Término de búsqueda para un ingrediente; si no está mapeado, usa una
    /// versión simplificada del nombre (sin paréntesis ni "/").
    func query(for ingredientName: String) -> String {
        if let q = ingredientQueries[ingredientName] { return q }
        var name = ingredientName.lowercased()
        if let range = name.range(of: "(") { name = String(name[..<range.lowerBound]) }
        if let range = name.range(of: "/") { name = String(name[..<range.lowerBound]) }
        return name.trimmingCharacters(in: .whitespaces)
    }
}

/// Decodifica de forma flexible la sección "queries" (o ignora "notes").
private struct AnyQueryMap: Decodable {
    let value: [String: String]?
    init(from decoder: Decoder) throws {
        let container = try decoder.singleValueContainer()
        value = try? container.decode([String: String].self)
    }
}
