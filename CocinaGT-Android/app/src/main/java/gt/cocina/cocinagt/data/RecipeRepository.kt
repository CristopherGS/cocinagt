package gt.cocina.cocinagt.data

import android.content.Context
import gt.cocina.cocinagt.model.Recipe
import gt.cocina.cocinagt.model.RecipeDatabase
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.contentOrNull

/** Carga y expone el catálogo de recetas y el mapeo de búsquedas desde assets. */
class RecipeRepository(context: Context) {

    private val json = Json { ignoreUnknownKeys = true }

    val database: RecipeDatabase = loadDatabase(context)
    val recipes: List<Recipe> get() = database.recipes

    /** Mapa de nombre de ingrediente -> término de búsqueda para la API de tiendas. */
    private val ingredientQueries: Map<String, String> = loadQueries(context)

    private fun loadDatabase(context: Context): RecipeDatabase = try {
        val text = context.assets.open("recipes.json").bufferedReader().use { it.readText() }
        json.decodeFromString(RecipeDatabase.serializer(), text)
    } catch (e: Exception) {
        RecipeDatabase()
    }

    private fun loadQueries(context: Context): Map<String, String> = try {
        val text = context.assets.open("ingredient_queries.json").bufferedReader().use { it.readText() }
        val obj = json.parseToJsonElement(text).jsonObject["queries"]?.jsonObject ?: return emptyMap()
        obj.mapValues { it.value.jsonPrimitive.contentOrNull.orEmpty() }
    } catch (e: Exception) {
        emptyMap()
    }

    fun recipeById(id: String): Recipe? = recipes.firstOrNull { it.id == id }

    /**
     * Término de búsqueda para un ingrediente; si no está mapeado usa una
     * versión simplificada del nombre (sin paréntesis ni "/").
     */
    fun queryFor(ingredientName: String): String {
        ingredientQueries[ingredientName]?.let { return it }
        var name = ingredientName.lowercase()
        name.indexOf('(').takeIf { it >= 0 }?.let { name = name.substring(0, it) }
        name.indexOf('/').takeIf { it >= 0 }?.let { name = name.substring(0, it) }
        return name.trim()
    }
}
