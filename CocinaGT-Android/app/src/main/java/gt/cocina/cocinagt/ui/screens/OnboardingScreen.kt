package gt.cocina.cocinagt.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import gt.cocina.cocinagt.model.UserPreferences
import gt.cocina.cocinagt.ui.AppViewModel
import gt.cocina.cocinagt.ui.gtq

@Composable
fun OnboardingScreen(vm: AppViewModel) {
    var step by remember { mutableIntStateOf(0) }
    val prefs = vm.prefs

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(24.dp))
        Text("🍳 CocinaGT", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary)
        Text("Planes de comida económicos para Guatemala",
            style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.Center)

        Column(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            when (step) {
                0 -> {
                    Text("¡Bienvenido!", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(12.dp))
                    Text(
                        "Te armamos un menú semanal con platillos chapines que se ajusta a tu presupuesto en Quetzales. Dinos un par de cosas para empezar.",
                        textAlign = TextAlign.Center
                    )
                }
                1 -> {
                    Text("¿Cuánto quieres gastar por semana?",
                        style = MaterialTheme.typography.titleLarge, textAlign = TextAlign.Center)
                    Spacer(Modifier.height(16.dp))
                    Text(gtq(prefs.weeklyBudget), style = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Slider(
                        value = prefs.weeklyBudget.toFloat(),
                        onValueChange = { vm.updatePrefs { p -> p.copy(weeklyBudget = it.toDouble()) } },
                        valueRange = 150f..1200f,
                        steps = 41
                    )
                }
                2 -> {
                    Text("¿Para cuántas personas cocinas?",
                        style = MaterialTheme.typography.titleLarge, textAlign = TextAlign.Center)
                    Spacer(Modifier.height(16.dp))
                    Text("${prefs.householdSize} persona${if (prefs.householdSize == 1) "" else "s"}",
                        style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(12.dp))
                    androidx.compose.foundation.layout.Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedButton(onClick = {
                            vm.updatePrefs { it.copy(householdSize = (it.householdSize - 1).coerceAtLeast(1)) }
                        }) { Text("–") }
                        OutlinedButton(onClick = {
                            vm.updatePrefs { it.copy(householdSize = (it.householdSize + 1).coerceAtMost(12)) }
                        }) { Text("+") }
                    }
                }
                else -> {
                    Text("¿Qué comidas planificamos?",
                        style = MaterialTheme.typography.titleLarge, textAlign = TextAlign.Center)
                    Spacer(Modifier.height(12.dp))
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
                        label = "Solo recetas vegetarianas",
                        checked = prefs.vegetarianOnly,
                        onChange = { on -> vm.updatePrefs { it.copy(vegetarianOnly = on) } }
                    )
                }
            }
        }

        Button(
            onClick = { if (step < 3) step++ else vm.completeOnboarding() },
            enabled = !(step == 3 && prefs.mealTypes.isEmpty()),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (step < 3) "Siguiente" else "Crear mi plan")
        }
    }
}

@Composable
internal fun ToggleRow(label: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    androidx.compose.foundation.layout.Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label)
        Switch(checked = checked, onCheckedChange = onChange)
    }
}
