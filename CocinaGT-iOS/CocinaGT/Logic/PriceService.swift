import Foundation

// MARK: - Modelos del JSON de la API de catálogo (VTEX)

private struct VTEXProduct: Decodable {
    let productName: String
    let items: [VTEXItem]
}
private struct VTEXItem: Decodable {
    let measurementUnit: String?
    let sellers: [VTEXSeller]
}
private struct VTEXSeller: Decodable {
    let commertialOffer: VTEXOffer   // 'commertialOffer' es el nombre real (typo de VTEX)
}
private struct VTEXOffer: Decodable {
    let Price: Double?
    let AvailableQuantity: Int?
}

/// Consulta precios reales desde la API pública de catálogo de las tiendas.
///
/// Como las apps son nativas, pueden llamar la API directamente (no hay CORS),
/// sin necesidad de un servidor propio. El "match" de ingrediente a producto es
/// aproximado: se busca el término, se puntúan los primeros resultados y se
/// elige el producto básico más probable.
struct PriceService {

    /// Palabras que indican que NO es el alimento buscado (utensilios, juguetes…).
    private static let blacklist = [
        "batidora", "calentador", "calent", "molde", "almacenador", "juguete",
        "cara de papa", "señor", "senor", "micro", "exprimidor", "rallador",
        "olla", "sarten", "sartén", "pelador", "picador", "recipiente", "tabla",
        "cuchillo", "bowl", "tazon", "tazón", "peluche", "disfraz", "maceta",
        "para sembrar", "sembrar"
    ]
    private static let foodUnits: Set<String> = ["kg", "g", "gr", "lt", "l", "ml", "un", "und", "unid"]

    private let session: URLSession

    init(session: URLSession = .shared) {
        self.session = session
    }

    /// Busca el mejor precio para un término en una tienda. Devuelve nil si no
    /// hay un match razonable (la app caerá al precio estimado).
    func bestPrice(host: String, query: String) async -> (productName: String, price: Double)? {
        guard let encoded = query.addingPercentEncoding(withAllowedCharacters: .urlPathAllowed),
              let url = URL(string: "https://\(host)/api/catalog_system/pub/products/search/\(encoded)?_from=0&_to=19")
        else { return nil }

        var request = URLRequest(url: url, timeoutInterval: 25)
        request.setValue("Mozilla/5.0 CocinaGT", forHTTPHeaderField: "User-Agent")

        guard let (data, response) = try? await session.data(for: request),
              let http = response as? HTTPURLResponse, (200...299).contains(http.statusCode),
              let products = try? JSONDecoder().decode([VTEXProduct].self, from: data)
        else { return nil }

        var best: (name: String, price: Double)?
        var bestScore = -Double.greatestFiniteMagnitude

        for (position, product) in products.enumerated() {
            guard let item = product.items.first,
                  let offer = item.sellers.first?.commertialOffer,
                  let price = offer.Price, price > 0,
                  (offer.AvailableQuantity ?? 0) > 0 else { continue }

            let s = Self.score(name: product.productName, query: query,
                               position: position, measurementUnit: item.measurementUnit, price: price)
            if s > bestScore {
                bestScore = s
                best = (product.productName, price)
            }
        }

        // Umbral mínimo de confianza.
        guard let best, bestScore > -40 else { return nil }
        return (best.name, best.price)
    }

    /// Consulta en paralelo los precios de varios ingredientes para una tienda.
    /// `items` es una lista de (nombreIngrediente, términoDeBúsqueda).
    func fetchPrices(store: Store, items: [(name: String, query: String)]) async -> [String: LivePrice] {
        await withTaskGroup(of: LivePrice?.self) { group in
            for item in items {
                group.addTask {
                    guard let result = await bestPrice(host: store.host, query: item.query) else { return nil }
                    return LivePrice(ingredientName: item.name,
                                     productName: result.productName,
                                     price: result.price,
                                     storeId: store.id)
                }
            }
            var out: [String: LivePrice] = [:]
            for await price in group {
                if let price { out[price.ingredientName] = price }
            }
            return out
        }
    }

    // MARK: - Puntuación

    private static func score(name: String, query: String, position: Int,
                              measurementUnit: String?, price: Double) -> Double {
        let n = name.lowercased()
        let words = query.lowercased().split(separator: " ").map(String.init)
        var s = 0.0

        if blacklist.contains(where: { n.contains($0) }) { s -= 100 }
        if let first = words.first, n.hasPrefix(first) { s += 10 }
        if words.allSatisfy({ n.contains($0) }) { s += 6 }
        else if let first = words.first, n.contains(first) { s += 3 }
        s -= Double(position) * 0.7
        if let mu = measurementUnit?.lowercased(), foodUnits.contains(mu) { s += 2 }
        if price > 150 { s -= 4 }
        return s
    }
}
