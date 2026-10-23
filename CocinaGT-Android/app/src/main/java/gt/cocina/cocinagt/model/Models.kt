package gt.cocina.cocinagt.model

import kotlinx.serialization.Serializable

/** Un ingrediente de una receta con su costo estimado en Quetzales (GTQ). */
@Serializable
data class Ingredient(
    val name: String,
    val quantity: Double,
    val unit: String,
    val group: String,
    /** Costo estimado de la cantidad indicada, en GTQ. */
    val cost: Double
)

/** Una receta guatemalteca con su información de costo y preparación. */
@Serializable
data class Recipe(
    val id: String,
    val name: String,
    val category: String,
    val servings: Int,
    val prepMinutes: Int,
    /** Costo aproximado por porción, en GTQ. */
    val costPerServing: Double,
    val tags: List<String> = emptyList(),
    val description: String,
    val ingredients: List<Ingredient> = emptyList(),
    val steps: List<String> = emptyList()
) {
    /** Costo total de preparar la receta completa (todas sus porciones). */
    val totalCost: Double get() = ingredients.sumOf { it.cost }
}

/** Contenedor del archivo recipes.json. */
@Serializable
data class RecipeDatabase(
    val currency: String = "GTQ",
    val currencySymbol: String = "Q",
    val country: String = "Guatemala",
    val categories: List<String> = emptyList(),
    val ingredientGroups: List<String> = emptyList(),
    val recipes: List<Recipe> = emptyList()
)

/** Preferencias del hogar que alimentan el generador de planes. */
@Serializable
data class UserPreferences(
    /** Presupuesto semanal disponible para comida, en GTQ. */
    val weeklyBudget: Double = 350.0,
    /** Número de personas en el hogar. */
    val householdSize: Int = 4,
    /** Tipos de comida que se desean planificar. */
    val mealTypes: Set<String> = setOf("Desayuno", "Almuerzo", "Cena"),
    /** Solo recetas vegetarianas. */
    val vegetarianOnly: Boolean = false,
    /** Indica si el usuario ya completó el onboarding. */
    val hasOnboarded: Boolean = false
) {
    companion object {
        val mealTypeOptions = listOf("Desayuno", "Almuerzo", "Cena")
    }
}

/** Una comida planificada para un día y tipo específico. */
@Serializable
data class PlannedMeal(
    val dayIndex: Int,     // 0 = Lunes ... 6 = Domingo
    val mealType: String,  // Desayuno / Almuerzo / Cena
    val recipeId: String,
    val servings: Int,
    /** Costo estimado de esta comida para el hogar, en GTQ. */
    val cost: Double
)

/** Plan semanal completo generado para el hogar. */
@Serializable
data class WeeklyPlan(
    val meals: List<PlannedMeal>,
    val budget: Double
) {
    val totalCost: Double get() = meals.sumOf { it.cost }
    val remaining: Double get() = budget - totalCost
    val isWithinBudget: Boolean get() = totalCost <= budget

    fun mealsForDay(day: Int): List<PlannedMeal> =
        meals.filter { it.dayIndex == day }
            .sortedBy { UserPreferences.mealTypeOptions.indexOf(it.mealType) }

    companion object {
        val dayNames = listOf("Lunes", "Martes", "Miércoles", "Jueves", "Viernes", "Sábado", "Domingo")
    }
}
