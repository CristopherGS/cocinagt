package gt.cocina.cocinagt.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.item
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import gt.cocina.cocinagt.ui.AppViewModel
import gt.cocina.cocinagt.ui.gtq

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipesScreen(vm: AppViewModel, onOpenRecipe: (String) -> Unit) {
    var query by remember { mutableStateOf("") }
    var category by remember { mutableStateOf<String?>(null) }

    val filtered = vm.allRecipes.filter { recipe ->
        (category == null || recipe.category == category) &&
            (query.isBlank() || recipe.name.contains(query, ignoreCase = true))
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Recetas chapinas") }) }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                label = { Text("Buscar receta") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)
            )

            Row(
                Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(selected = category == null, onClick = { category = null },
                    label = { Text("Todas") })
                vm.categories.forEach { cat ->
                    FilterChip(selected = category == cat, onClick = { category = cat },
                        label = { Text(cat) })
                }
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filtered) { recipe ->
                    Card(Modifier.fillMaxWidth().clickable { onOpenRecipe(recipe.id) }) {
                        Column(Modifier.padding(14.dp)) {
                            Text(recipe.name, style = MaterialTheme.typography.bodyLarge)
                            Text(
                                "${recipe.category} · ${recipe.prepMinutes} min · ${gtq(recipe.costPerServing)}/porción",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                    }
                }
                item { }
            }
        }
    }
}
