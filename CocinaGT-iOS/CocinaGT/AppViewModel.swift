import Foundation
import SwiftUI

/// Estado central de la app: preferencias, plan actual y lista de compras.
/// Persiste las preferencias y el plan en UserDefaults.
@MainActor
final class AppViewModel: ObservableObject {

    @Published var prefs: UserPreferences {
        didSet { persistPrefs() }
    }
    @Published var plan: WeeklyPlan? {
        didSet { persistPlan() }
    }

    /// Tienda seleccionada para precios en vivo; nil = usar precios estimados.
    @Published var selectedStoreId: String? {
        didSet { UserDefaults.standard.set(selectedStoreId, forKey: storeKey) }
    }
    /// Precios reales encontrados por ingrediente (clave = nombre del ingrediente).
    @Published var livePrices: [String: LivePrice] = [:]
    @Published var priceStatus: PriceStatus = .idle
    @Published private(set) var lastPriceUpdate: Date?

    enum PriceStatus: Equatable {
        case idle, loading, updated, failed
    }

    var selectedStore: Store? { Store.byId(selectedStoreId) }

    private let prefsKey = "cocinagt.prefs"
    private let planKey = "cocinagt.plan"
    private let storeKey = "cocinagt.store"

    private let generator = MealPlanGenerator()
    private let shoppingBuilder = ShoppingListBuilder()
    private let priceService = PriceService()

    init() {
        if let data = UserDefaults.standard.data(forKey: prefsKey),
           let decoded = try? JSONDecoder().decode(UserPreferences.self, from: data) {
            prefs = decoded
        } else {
            prefs = UserPreferences()
        }

        if let data = UserDefaults.standard.data(forKey: planKey),
           let decoded = try? JSONDecoder().decode(WeeklyPlan.self, from: data) {
            plan = decoded
        }

        selectedStoreId = UserDefaults.standard.string(forKey: storeKey)
    }

    // MARK: - Acciones

    func completeOnboarding() {
        prefs.hasOnboarded = true
        regeneratePlan()
    }

    func regeneratePlan() {
        plan = generator.makePlan(for: prefs)
    }

    func recipe(for meal: PlannedMeal) -> Recipe? {
        RecipeStore.shared.recipe(withId: meal.recipeId)
    }

    /// Secciones de la lista de compras, con precios reales si hay una tienda
    /// seleccionada y ya se actualizaron; si no, con precios estimados.
    func shoppingSections() -> [ShoppingSection] {
        guard let plan else { return [] }
        let prices = selectedStoreId != nil ? livePrices : [:]
        return shoppingBuilder.build(from: plan, livePrices: prices)
    }

    /// Cambia la tienda y limpia los precios anteriores (son de otra tienda).
    func selectStore(_ store: Store?) {
        selectedStoreId = store?.id
        livePrices = [:]
        lastPriceUpdate = nil
        priceStatus = .idle
    }

    /// Consulta precios reales en la tienda seleccionada para todos los
    /// ingredientes del plan y actualiza la lista de compras.
    func refreshPrices() async {
        guard let plan, let store = selectedStore else { return }
        priceStatus = .loading
        let items = shoppingBuilder.ingredientQueries(for: plan)
        let result = await priceService.fetchPrices(store: store, items: items)
        if result.isEmpty {
            priceStatus = .failed
        } else {
            livePrices = result
            lastPriceUpdate = Date()
            priceStatus = .updated
        }
    }

    // MARK: - Persistencia

    private func persistPrefs() {
        if let data = try? JSONEncoder().encode(prefs) {
            UserDefaults.standard.set(data, forKey: prefsKey)
        }
    }

    private func persistPlan() {
        if let plan, let data = try? JSONEncoder().encode(plan) {
            UserDefaults.standard.set(data, forKey: planKey)
        } else if plan == nil {
            UserDefaults.standard.removeObject(forKey: planKey)
        }
    }
}

// MARK: - Utilidades de formato

enum Money {
    /// Formatea un monto en Quetzales: 123.5 -> "Q123.50".
    static func gtq(_ amount: Double) -> String {
        String(format: "Q%.2f", amount)
    }
}
