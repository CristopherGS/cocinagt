package gt.cocina.cocinagt.logic

import gt.cocina.cocinagt.model.PlannedMeal
import gt.cocina.cocinagt.model.Recipe
import gt.cocina.cocinagt.model.UserPreferences
import gt.cocina.cocinagt.model.WeeklyPlan
import kotlin.math.roundToInt

/** Genera un plan semanal que intenta ajustarse al presupuesto del hogar. */
class MealPlanGenerator(private val recipes: List<Recipe>) {

    /**
     * Crea un plan semanal para las preferencias dadas.
     *
     * Estrategia:
     * 1. Filtra recetas por tipo de comida y dieta.
     * 2. Reparte el presupuesto semanal entre todas las comidas.
     * 3. Para cada día y tipo de comida elige una receta priorizando las que caben
     *    en el presupuesto por comida, rotando para variar el menú.
     */
    fun makePlan(prefs: UserPreferences): WeeklyPlan {
        val days = 7
        val mealTypes = UserPreferences.mealTypeOptions.filter { prefs.mealTypes.contains(it) }
        val totalMeals = (days * mealTypes.size).coerceAtLeast(1)
        val perMealBudget = prefs.weeklyBudget / totalMeals

        val meals = mutableListOf<PlannedMeal>()
        val rotation = mutableMapOf<String, Int>()

        for (day in 0 until days) {
            for (type in mealTypes) {
                val candidates = pool(type, prefs)
                if (candidates.isEmpty()) continue

                val chosen = choose(candidates, perMealBudget, prefs.householdSize, rotation, type)
                val cost = costForHousehold(chosen, prefs.householdSize)
                meals.add(
                    PlannedMeal(
                        dayIndex = day,
                        mealType = type,
                        recipeId = chosen.id,
                        servings = prefs.householdSize,
                        cost = cost
                    )
                )
            }
        }

        return WeeklyPlan(meals = meals, budget = prefs.weeklyBudget)
    }

    private fun pool(mealType: String, prefs: UserPreferences): List<Recipe> {
        var pool = recipes.filter { it.category == mealType }
        if (mealType == "Cena" && pool.size < 3) {
            pool = pool + recipes.filter { it.category == "Snack" }
        }
        if (prefs.vegetarianOnly) {
            pool = pool.filter { it.tags.contains("vegetariano") }
        }
        return pool.sortedBy { it.costPerServing }
    }

    private fun choose(
        candidates: List<Recipe>,
        perMealBudget: Double,
        householdSize: Int,
        rotation: MutableMap<String, Int>,
        mealType: String
    ): Recipe {
        val affordable = candidates.filter { costForHousehold(it, householdSize) <= perMealBudget }
        val working = affordable.ifEmpty { candidates }

        val index = (rotation[mealType] ?: 0) % working.size
        rotation[mealType] = index + 1
        return working[index]
    }

    /** Costo de cocinar una receta para todo el hogar (porción × personas), en GTQ. */
    fun costForHousehold(recipe: Recipe, householdSize: Int): Double =
        (recipe.costPerServing * householdSize * 100).roundToInt() / 100.0
}
