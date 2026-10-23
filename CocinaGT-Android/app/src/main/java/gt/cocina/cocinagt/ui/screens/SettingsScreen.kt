package gt.cocina.cocinagt.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import gt.cocina.cocinagt.model.UserPreferences
import gt.cocina.cocinagt.ui.AppViewModel
import gt.cocina.cocinagt.ui.gtq

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(vm: AppViewModel) {
    val prefs = vm.prefs

    Scaffold(
        topBar = { TopAppBar(title = { Text("Ajustes") }) }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Section("Presupuesto semanal") {
                Text(gtq(prefs.weeklyBudget), style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                Slider(
                    value = prefs.weeklyBudget.toFloat(),
                    onValueChange = { vm.updatePrefs { p -> p.copy(weeklyBudget = it.toDouble()) } },
                    valueRange = 150f..1200f, steps = 41
                )
            }

            Section("Hogar") {
                Row(verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    OutlinedButton(onClick = {
                        vm.updatePrefs { it.copy(householdSize = (it.householdSize - 1).coerceAtLeast(1)) }
                    }) { Text("–") }
                    Text("${prefs.householdSize} persona${if (prefs.householdSize == 1) "" else "s"}",
                        fontWeight = FontWeight.Bold)
                    OutlinedButton(onClick = {
                        vm.updatePrefs { it.copy(householdSize = (it.householdSize + 1).coerceAtMost(12)) }
                    }) { Text("+") }
                }
            }

            Section("Comidas a planificar") {
                UserPreferences.mealTypeOptions.forEach { type ->
                    ToggleRow(
                        label = type,
                        checked = prefs.mealTypes.contains(type),
                        onChange = { on ->
                            vm.updatePrefs {
                                val set = it.mealTypes.toMutableSet()
                                if (on) set.add(type) else set.remove(type)
                                it.copy(mealTypes = set)
                            }
                        }
                    )
                }
                ToggleRow(
                    label = "Solo vegetarianas",
                    checked = prefs.vegetarianOnly,
                    onChange = { on -> vm.updatePrefs { it.copy(vegetarianOnly = on) } }
                )
            }

            Button(
                onClick = { vm.regeneratePlan() },
                enabled = prefs.mealTypes.isNotEmpty(),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Filled.Refresh, contentDescription = null)
                Text("  Regenerar plan semanal")
            }

            Text("CocinaGT · Planes de comida económicos para Guatemala.\n" +
                "Precios en Quetzales (GTQ); son estimados de mercado y pueden variar.",
                style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
        }
    }
}

@Composable
private fun Section(title: String, content: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        content()
    }
}
