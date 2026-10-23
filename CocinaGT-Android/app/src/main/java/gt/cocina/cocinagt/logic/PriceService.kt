package gt.cocina.cocinagt.logic

import gt.cocina.cocinagt.model.LivePrice
import gt.cocina.cocinagt.model.Store
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

/**
 * Consulta precios reales desde la API pública de catálogo de las tiendas (VTEX).
 *
 * Al ser una app nativa puede llamar la API directamente (sin CORS ni servidor
 * propio). El "match" ingrediente -> producto es aproximado: se busca el término,
 * se puntúan los primeros resultados y se elige el producto básico más probable.
 */
class PriceService {

    private val json = Json { ignoreUnknownKeys = true }

    private val blacklist = listOf(
        "batidora", "calentador", "calent", "molde", "almacenador", "juguete",
        "cara de papa", "señor", "senor", "micro", "exprimidor", "rallador",
        "olla", "sarten", "sartén", "pelador", "picador", "recipiente", "tabla",
        "cuchillo", "bowl", "tazon", "tazón", "peluche", "disfraz", "maceta",
        "para sembrar", "sembrar"
    )
    private val foodUnits = setOf("kg", "g", "gr", "lt", "l", "ml", "un", "und", "unid")

    /** Consulta en paralelo los precios de varios ingredientes para una tienda. */
    suspend fun fetchPrices(
        store: Store,
        items: List<Pair<String, String>> // (nombreIngrediente, término)
    ): Map<String, LivePrice> = withContext(Dispatchers.IO) {
        coroutineScope {
            items.map { (name, query) ->
                async {
                    bestPrice(store.host, query)?.let { (product, price) ->
                        LivePrice(name, product, price, store.id)
                    }
                }
            }.awaitAll().filterNotNull().associateBy { it.ingredientName }
        }
    }

    /** Busca el mejor precio para un término; null si no hay match razonable. */
    private fun bestPrice(host: String, query: String): Pair<String, Double>? {
        val body = httpGet(
            "https://$host/api/catalog_system/pub/products/search/" +
                URLEncoder.encode(query, "UTF-8") + "?_from=0&_to=19"
        ) ?: return null

        val products = runCatching { json.parseToJsonElement(body).jsonArray }.getOrNull() ?: return null

        var best: Pair<String, Double>? = null
        var bestScore = -1e9
        products.forEachIndexed { position, element ->
            val obj = element.jsonObject
            val name = obj["productName"]?.jsonPrimitive?.contentOrNull ?: return@forEachIndexed
            val item = obj["items"]?.jsonArray?.firstOrNull()?.jsonObject ?: return@forEachIndexed
            val offer = item["sellers"]?.jsonArray?.firstOrNull()?.jsonObject
                ?.get("commertialOffer")?.jsonObject ?: return@forEachIndexed

            val price = offer["Price"]?.jsonPrimitive?.doubleOrNull ?: return@forEachIndexed
            val available = offer["AvailableQuantity"]?.jsonPrimitive?.doubleOrNull ?: 0.0
            if (price <= 0 || available <= 0) return@forEachIndexed

            val unit = item["measurementUnit"]?.jsonPrimitive?.contentOrNull
            val s = score(name, query, position, unit, price)
            if (s > bestScore) {
                bestScore = s
                best = name to price
            }
        }

        return if (best != null && bestScore > -40) best else null
    }

    private fun score(name: String, query: String, position: Int, unit: String?, price: Double): Double {
        val n = name.lowercase()
        val words = query.lowercase().split(" ").filter { it.isNotBlank() }
        var s = 0.0
        if (blacklist.any { n.contains(it) }) s -= 100
        val first = words.firstOrNull().orEmpty()
        if (first.isNotEmpty() && n.startsWith(first)) s += 10
        if (words.isNotEmpty() && words.all { n.contains(it) }) s += 6
        else if (first.isNotEmpty() && n.contains(first)) s += 3
        s -= position * 0.7
        if (unit != null && foodUnits.contains(unit.lowercase())) s += 2
        if (price > 150) s -= 4
        return s
    }

    private fun httpGet(urlString: String): String? = try {
        val conn = (URL(urlString).openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            connectTimeout = 15000
            readTimeout = 20000
            setRequestProperty("User-Agent", "Mozilla/5.0 CocinaGT")
            setRequestProperty("Accept", "application/json")
        }
        if (conn.responseCode in 200..299) {
            conn.inputStream.bufferedReader().use { it.readText() }
        } else null
    } catch (e: Exception) {
        null
    }
}
