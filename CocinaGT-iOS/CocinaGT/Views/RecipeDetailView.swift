import SwiftUI

/// Detalle de una receta: descripción, ingredientes (con costo) y pasos.
struct RecipeDetailView: View {
    let recipe: Recipe
    /// Porciones para las que se escala la vista (por defecto, las de la receta).
    var servings: Int?

    private var targetServings: Int { servings ?? recipe.servings }
    private var scale: Double { Double(targetServings) / Double(max(recipe.servings, 1)) }

    var body: some View {
        List {
            Section {
                Text(recipe.description)
                    .foregroundStyle(.secondary)
                HStack(spacing: 20) {
                    stat("Porciones", "\(targetServings)")
                    stat("Tiempo", "\(recipe.prepMinutes) min")
                    stat("Por porción", Money.gtq(recipe.costPerServing))
                }
                if !recipe.tags.isEmpty {
                    ScrollView(.horizontal, showsIndicators: false) {
                        HStack {
                            ForEach(recipe.tags, id: \.self) { tag in
                                Text(tag)
                                    .font(.caption).bold()
                                    .padding(.horizontal, 10).padding(.vertical, 5)
                                    .background(Color.cocinaGreen.opacity(0.15))
                                    .foregroundStyle(Color.cocinaGreen)
                                    .clipShape(Capsule())
                            }
                        }
                    }
                }
            }

            Section("Ingredientes") {
                ForEach(recipe.ingredients) { ing in
                    HStack {
                        Text(formattedQuantity(ing))
                            .foregroundStyle(.secondary)
                            .frame(width: 110, alignment: .leading)
                        Text(ing.name)
                        Spacer()
                        Text(Money.gtq(ing.cost * scale))
                            .font(.footnote).foregroundStyle(.secondary)
                    }
                }
            }

            Section("Preparación") {
                ForEach(Array(recipe.steps.enumerated()), id: \.offset) { index, step in
                    HStack(alignment: .top, spacing: 12) {
                        Text("\(index + 1)")
                            .font(.caption).bold()
                            .frame(width: 24, height: 24)
                            .background(Color.cocinaGreen)
                            .foregroundStyle(.white)
                            .clipShape(Circle())
                        Text(step)
                    }
                    .padding(.vertical, 2)
                }
            }

            Section {
                HStack {
                    Text("Costo total (\(targetServings) porciones)").bold()
                    Spacer()
                    Text(Money.gtq(recipe.totalCost * scale))
                        .bold().foregroundStyle(Color.cocinaGreen)
                }
            }
        }
        .navigationTitle(recipe.name)
        .navigationBarTitleDisplayMode(.inline)
    }

    private func stat(_ label: String, _ value: String) -> some View {
        VStack {
            Text(value).font(.headline)
            Text(label).font(.caption).foregroundStyle(.secondary)
        }
        .frame(maxWidth: .infinity)
    }

    private func formattedQuantity(_ ing: Ingredient) -> String {
        let q = ing.quantity * scale
        let qStr = q == q.rounded() ? String(format: "%.0f", q) : String(format: "%.1f", q)
        return "\(qStr) \(ing.unit)"
    }
}

#Preview {
    NavigationStack {
        RecipeDetailView(recipe: RecipeStore.shared.recipes.first!, servings: 4)
    }
}
