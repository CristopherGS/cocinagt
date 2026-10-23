package gt.cocina.cocinagt.logic

import gt.cocina.cocinagt.data.RecipeRepository
import gt.cocina.cocinagt.model.LivePrice
import gt.cocina.cocinagt.model.WeeklyPlan

/** Un renglón de la lista de compras: un ingrediente consolidado. */
data class ShoppingItem(
    val name: String,
    val group: String,
    /** Costo estimado (de mi dataset), usado como respaldo. */
    val estimatedCost: Double,
    /** En cuántas recetas del plan aparece este ingrediente. */
    val occurrences: Int,
    /** Precio real en la tienda (null si no hay o se usa estimado). */
    val livePrice: Double? = null,
    /** Producto representativo elegido en la tienda. */
    val liveProductName: String? = null
) {
    /** Costo que se usa en la lista: el real si existe, si no el estimado. */
    val effectiveCost: Double get() = livePrice ?: estimatedCost
    val isLive: Boolean get() = livePrice != null
}

/** Una sección de la lista de compras (por grupo de ingrediente). */
data class ShoppingSection(
    val group: String,
    val items: List<ShoppingItem>
) {
    val subtotal: Double get() = items.sumOf { it.effectiveCost }
}

/** Construye la lista de compras consolidada a partir de un plan semanal. */
class ShoppingListBuilder(private val repository: RecipeRepository) {

    fun build(plan: WeeklyPlan): List<ShoppingSection> {
        val merged = LinkedHashMap<String, ShoppingItem>()

        for (meal in plan.meals) {
            val recipe = repository.recipeById(meal.recipeId) ?: continue
            for (ing in recipe.ingredients) {
                val existing = merged[ing.name]
                merged[ing.name] = if (existing != null) {
                    existing.copy(
                        estimatedCost = existing.estimatedCost + ing.cost,
                        occurrences = existing.occurrences + 1
                    )
                } else {
                    ShoppingItem(ing.name, ing.group, ing.cost, 1)
                }
            }
        }

        val order = repository.database.ingredientGroups
        return merged.values
            .groupBy { it.group }
            .map { (group, items) -> ShoppingSection(group, items.sortedBy { it.name }) }
            .sortedBy { section ->
                order.indexOf(section.group).let { if (it == -1) 99 else it }
            }
    }

    fun total(sections: List<ShoppingSection>): Double = sections.sumOf { it.subtotal }

    /** Igual que [build], pero aplica precios reales donde existan. */
    fun build(plan: WeeklyPlan, livePrices: Map<String, LivePrice>): List<ShoppingSection> {
        val base = build(plan)
        if (livePrices.isEmpty()) return base
        return base.map { section ->
            section.copy(items = section.items.map { item ->
                val live = livePrices[item.name] ?: return@map item
                item.copy(livePrice = live.price, liveProductName = live.productName)
            })
        }
    }

    /** Lista de (nombre, término de búsqueda) de todos los ingredientes del plan. */
    fun ingredientQueries(plan: WeeklyPlan): List<Pair<String, String>> {
        val seen = LinkedHashSet<String>()
        for (meal in plan.meals) {
            val recipe = repository.recipeById(meal.recipeId) ?: continue
            recipe.ingredients.forEach { seen.add(it.name) }
        }
        return seen.map { it to repository.queryFor(it) }
    }
}
