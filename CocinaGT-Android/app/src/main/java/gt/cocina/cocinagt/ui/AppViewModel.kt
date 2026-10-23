package gt.cocina.cocinagt.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import gt.cocina.cocinagt.data.PreferencesStore
import gt.cocina.cocinagt.data.RecipeRepository
import gt.cocina.cocinagt.logic.MealPlanGenerator
import gt.cocina.cocinagt.logic.PriceService
import gt.cocina.cocinagt.logic.ShoppingListBuilder
import gt.cocina.cocinagt.logic.ShoppingSection
import gt.cocina.cocinagt.model.LivePrice
import gt.cocina.cocinagt.model.PlannedMeal
import gt.cocina.cocinagt.model.PriceStatus
import gt.cocina.cocinagt.model.Recipe
import gt.cocina.cocinagt.model.Store
import gt.cocina.cocinagt.model.UserPreferences
import gt.cocina.cocinagt.model.WeeklyPlan
import kotlinx.coroutines.launch

/** Estado central de la app: preferencias, plan actual y lista de compras. */
class AppViewModel(app: Application) : AndroidViewModel(app) {

    private val repository = RecipeRepository(app)
    private val store = PreferencesStore(app)
    private val generator = MealPlanGenerator(repository.recipes)
    private val shoppingBuilder = ShoppingListBuilder(repository)
    private val priceService = PriceService()

    var prefs by mutableStateOf(store.loadPreferences())
        private set

    var plan by mutableStateOf(store.loadPlan())
        private set

    /** Tienda seleccionada para precios en vivo; null = usar estimados. */
    var selectedStoreId by mutableStateOf(store.loadStoreId())
        private set
    var livePrices by mutableStateOf<Map<String, LivePrice>>(emptyMap())
        private set
    var priceStatus by mutableStateOf(PriceStatus.IDLE)
        private set
    var lastPriceUpdate by mutableStateOf<Long?>(null)
        private set

    val selectedStore: Store? get() = Store.byId(selectedStoreId)

    val categories: List<String> get() = repository.database.categories
    val allRecipes: List<Recipe> get() = repository.recipes

    fun updatePrefs(transform: (UserPreferences) -> UserPreferences) {
        prefs = transform(prefs)
        store.savePreferences(prefs)
    }

    fun completeOnboarding() {
        updatePrefs { it.copy(hasOnboarded = true) }
        regeneratePlan()
    }

    fun regeneratePlan() {
        val newPlan = generator.makePlan(prefs)
        plan = newPlan
        store.savePlan(newPlan)
    }

    fun recipeFor(meal: PlannedMeal): Recipe? = repository.recipeById(meal.recipeId)

    fun recipeById(id: String): Recipe? = repository.recipeById(id)

    fun shoppingSections(): List<ShoppingSection> {
        val p = plan ?: return emptyList()
        val prices = if (selectedStoreId != null) livePrices else emptyMap()
        return shoppingBuilder.build(p, prices)
    }

    fun shoppingTotal(sections: List<ShoppingSection>): Double = shoppingBuilder.total(sections)

    /** Cambia la tienda y limpia los precios anteriores (eran de otra tienda). */
    fun selectStore(storeId: String?) {
        selectedStoreId = storeId
        store.saveStoreId(storeId)
        livePrices = emptyMap()
        lastPriceUpdate = null
        priceStatus = PriceStatus.IDLE
    }

    /** Consulta precios reales en la tienda seleccionada y actualiza la lista. */
    fun refreshPrices() {
        val p = plan ?: return
        val selected = selectedStore ?: return
        priceStatus = PriceStatus.LOADING
        viewModelScope.launch {
            val items = shoppingBuilder.ingredientQueries(p)
            val result = priceService.fetchPrices(selected, items)
            if (result.isEmpty()) {
                priceStatus = PriceStatus.FAILED
            } else {
                livePrices = result
                lastPriceUpdate = System.currentTimeMillis()
                priceStatus = PriceStatus.UPDATED
            }
        }
    }
}

/** Formatea un monto en Quetzales: 123.5 -> "Q123.50". */
fun gtq(amount: Double): String = "Q%.2f".format(amount)
