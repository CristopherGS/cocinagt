import SwiftUI

/// Lista de compras consolidada del plan, agrupada por tipo de ingrediente,
/// con precios estimados o reales (según la tienda elegida) y total en Quetzales.
struct ShoppingListView: View {
    @EnvironmentObject var model: AppViewModel
    @State private var checked: Set<String> = []

    var body: some View {
        NavigationStack {
            let sections = model.shoppingSections()
            Group {
                if sections.isEmpty {
                    ContentUnavailableView("Sin lista todavía",
                                           systemImage: "cart",
                                           description: Text("Genera un plan para ver tu lista de compras."))
                } else {
                    List {
                        storePicker
                        totalHeader(sections)
                        ForEach(sections) { section in
                            Section {
                                ForEach(section.items) { item in
                                    itemRow(item)
                                }
                            } header: {
                                HStack {
                                    Text(section.group)
                                    Spacer()
                                    Text(Money.gtq(section.subtotal))
                                        .foregroundStyle(.secondary)
                                }
                            }
                        }
                    }
                }
            }
            .navigationTitle("Lista de compras")
        }
    }

    // MARK: - Selector de tienda y precios

    private var storePicker: some View {
        Section {
            Picker("Tienda", selection: Binding(
                get: { model.selectedStoreId },
                set: { model.selectStore(Store.byId($0)) }
            )) {
                Text("Estimado").tag(String?.none)
                ForEach(Store.all) { store in
                    Text(store.name).tag(String?.some(store.id))
                }
            }

            if model.selectedStore != nil {
                Button {
                    Task { await model.refreshPrices() }
                } label: {
                    HStack {
                        if model.priceStatus == .loading {
                            ProgressView()
                            Text("  Consultando precios…")
                        } else {
                            Image(systemName: "arrow.triangle.2.circlepath")
                            Text("Actualizar precios")
                        }
                    }
                }
                .disabled(model.priceStatus == .loading)

                priceStatusText
            } else {
                Text("Precios estimados de mercado. Elige una tienda para traer precios reales en línea.")
                    .font(.footnote).foregroundStyle(.secondary)
            }
        } header: {
            Text("Precios")
        }
    }

    @ViewBuilder
    private var priceStatusText: some View {
        switch model.priceStatus {
        case .updated:
            Text("Precios reales de \(model.selectedStore?.name ?? ""). Toca un renglón para ver el producto usado.")
                .font(.footnote).foregroundStyle(Color.cocinaGreen)
        case .failed:
            Text("No se pudieron traer los precios (¿sin internet?). Se muestran los estimados.")
                .font(.footnote).foregroundStyle(.red)
        case .loading:
            EmptyView()
        case .idle:
            Text("Toca «Actualizar precios» para traer los precios reales de \(model.selectedStore?.name ?? "").")
                .font(.footnote).foregroundStyle(.secondary)
        }
    }

    // MARK: - Total y renglones

    private func totalHeader(_ sections: [ShoppingSection]) -> some View {
        Section {
            HStack {
                VStack(alignment: .leading) {
                    Text(model.priceStatus == .updated ? "Total real estimado" : "Total estimado")
                        .font(.caption).foregroundStyle(.secondary)
                    Text(Money.gtq(ShoppingListBuilder().total(of: sections)))
                        .font(.title2).bold()
                        .foregroundStyle(Color.cocinaGreen)
                    if let date = model.lastPriceUpdate, model.priceStatus == .updated {
                        Text("Actualizado \(date.formatted(date: .abbreviated, time: .shortened))")
                            .font(.caption2).foregroundStyle(.secondary)
                    }
                }
                Spacer()
                Image(systemName: "cart.fill")
                    .font(.largeTitle)
                    .foregroundStyle(Color.cocinaGreen.opacity(0.3))
            }
        }
    }

    private func itemRow(_ item: ShoppingItem) -> some View {
        Button {
            if checked.contains(item.id) { checked.remove(item.id) }
            else { checked.insert(item.id) }
        } label: {
            HStack(alignment: .top) {
                Image(systemName: checked.contains(item.id) ? "checkmark.circle.fill" : "circle")
                    .foregroundStyle(checked.contains(item.id) ? Color.cocinaGreen : .secondary)
                VStack(alignment: .leading, spacing: 2) {
                    HStack {
                        Text(item.name)
                            .strikethrough(checked.contains(item.id))
                            .foregroundStyle(checked.contains(item.id) ? .secondary : .primary)
                        if item.occurrences > 1 {
                            Text("×\(item.occurrences)")
                                .font(.caption).foregroundStyle(.secondary)
                        }
                    }
                    if let product = item.liveProductName {
                        Text(product)
                            .font(.caption).foregroundStyle(.secondary)
                            .lineLimit(1)
                    } else if model.selectedStore != nil && model.priceStatus == .updated {
                        Text("sin match · precio estimado")
                            .font(.caption2).foregroundStyle(.orange)
                    }
                }
                Spacer()
                Text(Money.gtq(item.effectiveCost))
                    .font(.subheadline)
                    .foregroundStyle(item.isLive ? Color.cocinaGreen : .secondary)
            }
        }
        .buttonStyle(.plain)
    }
}

#Preview {
    let m = AppViewModel()
    m.regeneratePlan()
    return ShoppingListView().environmentObject(m)
}
