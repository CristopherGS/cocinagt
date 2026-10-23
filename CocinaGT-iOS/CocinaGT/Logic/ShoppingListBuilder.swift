import Foundation

/// Un renglón de la lista de compras: un ingrediente agrupado con su costo acumulado.
struct ShoppingItem: Identifiable, Hashable {
    var id: String { name }
    let name: String
    let group: String
    /// Costo estimado (de mi dataset), usado como respaldo.
    var estimatedCost: Double
    /// En cuántas recetas del plan aparece este ingrediente.
    var occurrences: Int
    /// Precio real encontrado en la tienda (nil si no hay o se usa estimado).
    var livePrice: Double? = nil
    /// Producto representativo elegido en la tienda (para mostrarlo al usuario).
    var liveProductName: String? = nil

    /// Costo que se usa en la lista: el real si existe, si no el estimado.
    var effectiveCost: Double { livePrice ?? estimatedCost }
    var isLive: Bool { livePrice != nil }
}

/// Una sección de la lista de compras (por grupo de ingrediente).
struct ShoppingSection: Identifiable, Hashable {
    var id: String { group }
    let group: String
    let items: [ShoppingItem]

    var subtotal: Double { items.reduce(0) { $0 + $1.effectiveCost } }
}

/// Construye la lista de compras consolidada a partir de un plan semanal.
struct ShoppingListBuilder {

    let store: RecipeStore

    init(store: RecipeStore = .shared) {
        self.store = store
    }

    /// Agrupa todos los ingredientes del plan por grupo y acumula sus costos.
    func build(from plan: WeeklyPlan) -> [ShoppingSection] {
        var merged: [String: ShoppingItem] = [:]

        for meal in plan.meals {
            guard let recipe = store.recipe(withId: meal.recipeId) else { continue }
            for ing in recipe.ingredients {
                if var existing = merged[ing.name] {
                    existing.estimatedCost += ing.cost
                    existing.occurrences += 1
                    merged[ing.name] = existing
                } else {
                    merged[ing.name] = ShoppingItem(name: ing.name,
                                                    group: ing.group,
                                                    estimatedCost: ing.cost,
                                                    occurrences: 1)
                }
            }
        }

        let grouped = Dictionary(grouping: merged.values) { $0.group }
        let order = store.database.ingredientGroups

        return grouped
            .map { group, items in
                ShoppingSection(group: group,
                                items: items.sorted { $0.name < $1.name })
            }
            .sorted { lhs, rhs in
                (order.firstIndex(of: lhs.group) ?? 99) < (order.firstIndex(of: rhs.group) ?? 99)
            }
    }

    /// Igual que `build`, pero aplica precios reales cuando existen para un ingrediente.
    func build(from plan: WeeklyPlan, livePrices: [String: LivePrice]) -> [ShoppingSection] {
        let base = build(from: plan)
        guard !livePrices.isEmpty else { return base }
        return base.map { section in
            ShoppingSection(group: section.group, items: section.items.map { item in
                guard let live = livePrices[item.name] else { return item }
                var updated = item
                updated.livePrice = live.price
                updated.liveProductName = live.productName
                return updated
            })
        }
    }

    func total(of sections: [ShoppingSection]) -> Double {
        sections.reduce(0) { $0 + $1.subtotal }
    }

    /// Lista de (nombre, término de búsqueda) de todos los ingredientes del plan.
    func ingredientQueries(for plan: WeeklyPlan) -> [(name: String, query: String)] {
        var names: [String] = []
        var seen = Set<String>()
        for meal in plan.meals {
            guard let recipe = store.recipe(withId: meal.recipeId) else { continue }
            for ing in recipe.ingredients where !seen.contains(ing.name) {
                seen.insert(ing.name)
                names.append(ing.name)
            }
        }
        return names.map { (name: $0, query: store.query(for: $0)) }
    }
}
