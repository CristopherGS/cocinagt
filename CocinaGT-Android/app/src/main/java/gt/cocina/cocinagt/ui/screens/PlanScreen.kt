package gt.cocina.cocinagt.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.item
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import gt.cocina.cocinagt.model.PlannedMeal
import gt.cocina.cocinagt.model.WeeklyPlan
import gt.cocina.cocinagt.ui.AppViewModel
import gt.cocina.cocinagt.ui.gtq

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlanScreen(vm: AppViewModel, onOpenRecipe: (String) -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mi plan semanal") },
                actions = {
                    IconButton(onClick = { vm.regeneratePlan() }) {
                        Icon(Icons.Filled.Refresh, contentDescription = "Regenerar")
                    }
                }
            )
        }
    ) { padding ->
        val plan = vm.plan
        if (plan == null || plan.meals.isEmpty()) {
            EmptyState("Aún no hay plan", "Genera tu primer plan semanal con el botón de recargar.",
                Modifier.padding(padding))
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item { BudgetSummary(plan) }
            for (day in 0 until 7) {
                val meals = plan.mealsForDay(day)
                if (meals.isNotEmpty()) {
                    item {
                        Text(
                            WeeklyPlan.dayNames[day],
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                    items(meals) { meal -> MealRow(vm, meal, onOpenRecipe) }
                }
            }
            item { Spacer(Modifier.height(16.dp)) }
        }
    }
}

@Composable
private fun BudgetSummary(plan: WeeklyPlan) {
    Card(modifier = Modifier.fillMaxWidth().padding(top = 12.dp)) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            LabeledValue("Presupuesto", gtq(plan.budget))
            LabeledValue(
                "Costo estimado", gtq(plan.totalCost),
                color = if (plan.isWithinBudget) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
            )
            LinearProgressIndicator(
                progress = { (plan.totalCost / plan.budget).coerceIn(0.0, 1.0).toFloat() },
                modifier = Modifier.fillMaxWidth()
            )
            Text(
                if (plan.isWithinBudget) "Te quedan ${gtq(plan.remaining)} de margen 🎉"
                else "Te pasas por ${gtq(-plan.remaining)}. Prueba regenerar o planifica menos comidas.",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
private fun LabeledValue(label: String, value: String, color: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onSurface) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label)
        Text(value, fontWeight = FontWeight.Bold, color = color)
    }
}

@Composable
private fun MealRow(vm: AppViewModel, meal: PlannedMeal, onOpenRecipe: (String) -> Unit) {
    val recipe = vm.recipeFor(meal) ?: return
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onOpenRecipe(recipe.id) }
    ) {
        Row(
            Modifier.fillMaxWidth().padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(meal.mealType, style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline)
                Text(recipe.name, style = MaterialTheme.typography.bodyLarge)
            }
            Text(gtq(meal.cost), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        }
    }
}
