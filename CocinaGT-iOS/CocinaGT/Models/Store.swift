import Foundation

/// Una tienda de la que se pueden consultar precios en vivo.
struct Store: Identifiable, Hashable {
    let id: String
    let name: String
    /// Dominio que expone la API pública de catálogo (VTEX).
    let host: String

    /// Fuentes reales con precios distintos en Guatemala.
    /// Walmart, Paiz y Maxi Despensa comparten el mismo catálogo y precio,
    /// así que se representan con una sola opción "Walmart / Paiz".
    static let all: [Store] = [
        Store(id: "walmart", name: "Walmart / Paiz", host: "www.walmart.com.gt"),
        Store(id: "latorre", name: "La Torre", host: "www.latorre.com.gt")
    ]

    static func byId(_ id: String?) -> Store? {
        guard let id else { return nil }
        return all.first { $0.id == id }
    }
}

/// Precio en vivo encontrado para un ingrediente en una tienda.
struct LivePrice: Codable, Hashable {
    let ingredientName: String
    /// Producto representativo que la app eligió en la tienda.
    let productName: String
    let price: Double
    let storeId: String
}
