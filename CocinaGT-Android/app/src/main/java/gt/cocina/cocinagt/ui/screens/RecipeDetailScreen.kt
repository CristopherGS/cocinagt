package gt.cocina.cocinagt.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.item
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import gt.cocina.cocinagt.model.Ingredient
import gt.cocina.cocinagt.model.Recipe
import gt.cocina.cocinagt.ui.AppViewModel
import gt.cocina.cocinagt.ui.gtq

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeDetailScreen(vm: AppViewModel, recipeId: String, onBack: () -> Unit) {
    val recipe = vm.recipeById(recipeId)
    val household = vm.prefs.householdSize
    val scale = if (recipe != null) household.toDouble() / recipe.servings.coerceAtLeast(1) else 1.0

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(recipe?.name ?: "Receta") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás")
                    }
                }
            )
        }
    ) { padding ->
        if (recipe == null) {
            EmptyState("Receta no encontrada", "", Modifier.padding(padding))
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item { Text(recipe.description, color = MaterialTheme.colorScheme.outline) }
            item { StatsRow(recipe, household) }
            if (recipe.tags.isNotEmpty()) {
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        recipe.tags.take(4).forEach { tag ->
                            AssistChip(onClick = {}, label = { Text(tag) })
                        }
                    }
                }
            }

            item { SectionTitle("Ingredientes") }
            items(recipe.ingredients) { ing -> IngredientRow(ing, scale) }

            item { SectionTitle("Preparación") }
            itemsIndexed(recipe.steps) { index, step -> StepRow(index + 1, step) }

            item { HorizontalDivider() }
            item {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Costo total ($household porciones)", fontWeight = FontWeight.Bold)
                    Text(gtq(recipe.totalCost * scale), fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }
}

@Composable
private fun StatsRow(recipe: Recipe, household: Int) {
    Card(Modifier.fillMaxWidth()) {
        Row(Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceAround) {
            Stat("Porciones", household.toString())
            Stat("Tiempo", "${recipe.prepMinutes} min")
            Stat("Por porción", gtq(recipe.costPerServing))
        }
    }
}

@Composable
private fun Stat(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(text, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(top = 8.dp))
}

@Composable
private fun IngredientRow(ing: Ingredient, scale: Double) {
    val q = ing.quantity * scale
    val qStr = if (q == kotlin.math.floor(q)) "%.0f".format(q) else "%.1f".format(q)
    Row(Modifier.fillMaxWidth().padding(vertical = 2.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Text("$qStr ${ing.unit}  ${ing.name}")
        Text(gtq(ing.cost * scale), color = MaterialTheme.colorScheme.outline)
    }
}

@Composable
private fun StepRow(number: Int, step: String) {
    Row(Modifier.fillMaxWidth().padding(vertical = 2.dp)) {
        Surface(
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp).clip(CircleShape)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(number.toString(), color = MaterialTheme.colorScheme.onPrimary,
                    style = MaterialTheme.typography.labelMedium)
            }
        }
        Text(step, modifier = Modifier.padding(start = 12.dp))
    }
}
