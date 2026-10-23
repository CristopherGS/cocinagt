# CocinaGT 🍳🇬🇹

**Planes de comida económicos para Guatemala.**

CocinaGT es una app de planificación de comidas que genera un menú semanal de platillos chapines ajustado a tu presupuesto en **Quetzales (GTQ)** y te arma automáticamente la lista de compras. Está construida como **dos apps nativas independientes**:

| Plataforma | Carpeta | Stack |
|-----------|---------|-------|
| 📱 iOS | [`CocinaGT-iOS/`](./CocinaGT-iOS) | Swift + SwiftUI (Xcode 16, iOS 17+) |
| 🤖 Android | [`CocinaGT-Android/`](./CocinaGT-Android) | Kotlin + Jetpack Compose (AGP 8.7, minSdk 26) |

Ambas apps comparten el mismo catálogo de recetas: [`shared/recipes.json`](./shared/recipes.json).

## ¿Qué hace?

1. **Onboarding** — defines tu presupuesto semanal, cuántas personas comen en casa, qué comidas quieres planificar y si prefieres recetas vegetarianas.
2. **Plan semanal** — genera 7 días de menús intentando no pasarse de tu presupuesto, con una barra que muestra cuánto gastas vs. tu límite.
3. **Recetas chapinas** — pepián, hilachas, frijoles volteados, tamalitos, chow mein guatemalteco, rellenitos y más, con ingredientes y pasos, y costo escalado según el tamaño de tu hogar.
4. **Lista de compras** — consolida todos los ingredientes del plan agrupados por tipo (granos, verduras, carnes, lácteos…), con subtotales y total estimado en Quetzales, y casillas para ir marcando.

## Correr las apps

**iOS**
```bash
cd CocinaGT-iOS
open CocinaGT.xcodeproj   # ⌘R en un simulador de iPhone
```

**Android**
```bash
cd CocinaGT-Android
./gradlew assembleDebug    # o abre la carpeta en Android Studio y presiona Run
```

## Concepto y precios

CocinaGT es una app **original** inspirada en el concepto general de un planificador de comidas por presupuesto, construida y localizada específicamente para Guatemala. No contiene código, recursos ni marca de terceros.

Los precios de `recipes.json` son **estimados de mercado/tienda en Guatemala** y pueden variar por temporada y región. Puedes editar ese archivo (y las copias en cada app) para ajustarlos a tu zona o agregar tus propias recetas.

## Estructura del repositorio

```
shared/recipes.json       # Catálogo de recetas (fuente de verdad)
CocinaGT-iOS/             # App iOS (SwiftUI)
CocinaGT-Android/         # App Android (Jetpack Compose)
```
