# CocinaGT — iOS 📱

App iOS nativa (SwiftUI) de **CocinaGT**, planificador de comidas económicas para Guatemala. Genera un menú semanal de platillos chapines que se ajusta a tu presupuesto en Quetzales (GTQ) y arma tu lista de compras.

## Requisitos

- **Xcode 16 o superior** (probado para Xcode 26 / SDK de iOS más reciente). Usa *file system synchronized groups*.
- **iOS 18.0+** como objetivo de despliegue (optimizado para el iOS más reciente).

## Cómo abrir y correr

```bash
cd CocinaGT-iOS
open CocinaGT.xcodeproj
```

Selecciona un simulador de iPhone y presiona ⌘R.

Desde línea de comandos:

```bash
xcodebuild -project CocinaGT.xcodeproj -scheme CocinaGT \
  -destination 'platform=iOS Simulator,name=iPhone 15' build
```

## Estructura

```
CocinaGT/
├── CocinaGTApp.swift        # Punto de entrada + colores de marca
├── AppViewModel.swift       # Estado central + persistencia (UserDefaults)
├── Models/                  # Recipe, MealPlan, UserPreferences
├── Data/
│   ├── RecipeStore.swift    # Carga recipes.json desde el bundle
│   └── recipes.json         # Catálogo de recetas chapinas con precios en GTQ
├── Logic/
│   ├── MealPlanGenerator.swift   # Arma el plan semanal según presupuesto
│   └── ShoppingListBuilder.swift # Consolida la lista de compras
└── Views/                   # Onboarding, plan, recetas, compras, ajustes
```

## Funcionalidades

- **Onboarding**: presupuesto semanal, tamaño del hogar, comidas y dieta.
- **Plan semanal**: 7 días de menús que intentan respetar tu presupuesto, con barra de gasto.
- **Detalle de receta**: ingredientes con costo (escalado por porciones) y pasos.
- **Lista de compras**: agrupada por tipo de ingrediente, con subtotales y total.
- **Catálogo**: todas las recetas con búsqueda y filtro por categoría.
- **Ajustes**: cambia tus preferencias y regenera el plan.

## Datos de precios

Los precios (`recipes.json`) son **estimados de mercado/tienda en Guatemala en Quetzales** y pueden variar por temporada y región. El mismo dataset se comparte con la app de Android (`../shared/recipes.json`).

> App original inspirada en el concepto de planificador de comidas por presupuesto, construida y localizada para Guatemala. No contiene código ni recursos de terceros.
