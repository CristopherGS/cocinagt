package gt.cocina.cocinagt.data

import android.content.Context
import gt.cocina.cocinagt.model.UserPreferences
import gt.cocina.cocinagt.model.WeeklyPlan
import kotlinx.serialization.json.Json

/** Persiste preferencias y el plan actual en SharedPreferences, serializados a JSON. */
class PreferencesStore(context: Context) {

    private val prefs = context.getSharedPreferences("cocinagt", Context.MODE_PRIVATE)
    private val json = Json { ignoreUnknownKeys = true }

    fun loadPreferences(): UserPreferences {
        val raw = prefs.getString(KEY_PREFS, null) ?: return UserPreferences()
        return runCatching { json.decodeFromString(UserPreferences.serializer(), raw) }
            .getOrDefault(UserPreferences())
    }

    fun savePreferences(value: UserPreferences) {
        prefs.edit()
            .putString(KEY_PREFS, json.encodeToString(UserPreferences.serializer(), value))
            .apply()
    }

    fun loadPlan(): WeeklyPlan? {
        val raw = prefs.getString(KEY_PLAN, null) ?: return null
        return runCatching { json.decodeFromString(WeeklyPlan.serializer(), raw) }.getOrNull()
    }

    fun savePlan(plan: WeeklyPlan?) {
        val editor = prefs.edit()
        if (plan == null) {
            editor.remove(KEY_PLAN)
        } else {
            editor.putString(KEY_PLAN, json.encodeToString(WeeklyPlan.serializer(), plan))
        }
        editor.apply()
    }

    fun loadStoreId(): String? = prefs.getString(KEY_STORE, null)

    fun saveStoreId(id: String?) {
        prefs.edit().apply {
            if (id == null) remove(KEY_STORE) else putString(KEY_STORE, id)
        }.apply()
    }

    private companion object {
        const val KEY_PREFS = "preferences"
        const val KEY_PLAN = "plan"
        const val KEY_STORE = "store"
    }
}
