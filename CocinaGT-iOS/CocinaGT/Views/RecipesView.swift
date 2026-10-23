import SwiftUI

/// Catálogo completo de recetas, con búsqueda y filtro por categoría.
struct RecipesView: View {
    @State private var query = ""
    @State private var category: String? = nil

    private let store = RecipeStore.shared

    private var filtered: [Recipe] {
        store.recipes.filter { recipe in
            (category == nil || recipe.category == category) &&
            (query.isEmpty || recipe.name.localizedCaseInsensitiveContains(query))
        }
    }

    var body: some View {
        NavigationStack {
            List {
                categoryPicker
                ForEach(filtered) { recipe in
                    NavigationLink {
                        RecipeDetailView(recipe: recipe)
                    } label: {
                        VStack(alignment: .leading, spacing: 3) {
                            Text(recipe.name)
                            HStack(spacing: 8) {
                                Text(recipe.category)
                                Text("·")
                                Text("\(recipe.prepMinutes) min")
                                Text("·")
                                Text("\(Money.gtq(recipe.costPerServing))/porción")
                            }
                            .font(.caption).foregroundStyle(.secondary)
                        }
                    }
                }
            }
            .navigationTitle("Recetas chapinas")
            .searchable(text: $query, prompt: "Buscar receta")
        }
    }

    private var categoryPicker: some View {
        ScrollView(.horizontal, showsIndicators: false) {
            HStack {
                chip(title: "Todas", selected: category == nil) { category = nil }
                ForEach(store.database.categories, id: \.self) { cat in
                    chip(title: cat, selected: category == cat) { category = cat }
                }
            }
        }
        .listRowInsets(EdgeInsets())
        .listRowBackground(Color.clear)
        .padding(.vertical, 4)
    }

    private func chip(title: String, selected: Bool, action: @escaping () -> Void) -> some View {
        Button(action: action) {
            Text(title)
                .font(.subheadline).bold()
                .padding(.horizontal, 14).padding(.vertical, 7)
                .background(selected ? Color.cocinaGreen : Color(.secondarySystemBackground))
                .foregroundStyle(selected ? .white : .primary)
                .clipShape(Capsule())
        }
        .buttonStyle(.plain)
    }
}

#Preview {
    RecipesView()
}
