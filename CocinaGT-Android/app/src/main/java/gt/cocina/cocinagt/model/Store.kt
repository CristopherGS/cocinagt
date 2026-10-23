package gt.cocina.cocinagt.model

/** Una tienda de la que se pueden consultar precios en vivo. */
data class Store(
    val id: String,
    val name: String,
    /** Dominio que expone la API pública de catálogo (VTEX). */
    val host: String
) {
    companion object {
        /**
         * Fuentes reales con precios distintos en Guatemala.
         * Walmart, Paiz y Maxi Despensa comparten catálogo y precio, por eso
         * se representan con una sola opción.
         */
        val all = listOf(
            Store("walmart", "Walmart / Paiz", "www.walmart.com.gt"),
            Store("latorre", "La Torre", "www.latorre.com.gt")
        )

        fun byId(id: String?): Store? = all.firstOrNull { it.id == id }
    }
}

/** Precio en vivo encontrado para un ingrediente en una tienda. */
data class LivePrice(
    val ingredientName: String,
    /** Producto representativo que la app eligió en la tienda. */
    val productName: String,
    val price: Double,
    val storeId: String
)

/** Estado de la consulta de precios. */
enum class PriceStatus { IDLE, LOADING, UPDATED, FAILED }
