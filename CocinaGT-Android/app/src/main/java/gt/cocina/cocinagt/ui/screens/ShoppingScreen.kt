package gt.cocina.cocinagt.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.item
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.outlined.Circle
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import gt.cocina.cocinagt.logic.ShoppingItem
import gt.cocina.cocinagt.model.PriceStatus
import gt.cocina.cocinagt.model.Store
import gt.cocina.cocinagt.ui.AppViewModel
import gt.cocina.cocinagt.ui.gtq
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShoppingScreen(vm: AppViewModel) {
    val sections = vm.shoppingSections()
    val checked = remember { mutableStateMapOf<String, Boolean>() }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Lista de compras") }) }
    ) { padding ->
        if (sections.isEmpty()) {
            EmptyState("Sin lista todavía", "Genera un plan para ver tu lista de compras.",
                Modifier.padding(padding))
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            item { StorePicker(vm) }
            item { TotalCard(vm, vm.shoppingTotal(sections)) }

            sections.forEach { section ->
                item {
                    Row(Modifier.fillMaxWidth().padding(top = 10.dp, bottom = 2.dp),
                        horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(section.group, fontWeight = FontWeight.Bold)
                        Text(gtq(section.subtotal), color = MaterialTheme.colorScheme.outline)
                    }
                }
                items(section.items.size) { idx ->
                    val item = section.items[idx]
                    ShoppingRow(
                        item = item,
                        storeSelected = vm.selectedStore != null,
                        updated = vm.priceStatus == PriceStatus.UPDATED,
                        checked = checked[item.name] == true,
                        onToggle = { checked[item.name] = !(checked[item.name] ?: false) }
                    )
                }
            }
        }
    }
}

@Composable
private fun StorePicker(vm: AppViewModel) {
    Column(Modifier.fillMaxWidth().padding(vertical = 8.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text("Precios", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
        Row(
            Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(selected = vm.selectedStoreId == null, onClick = { vm.selectStore(null) },
                label = { Text("Estimado") })
            Store.all.forEach { s ->
                FilterChip(selected = vm.selectedStoreId == s.id, onClick = { vm.selectStore(s.id) },
                    label = { Text(s.name) })
            }
        }

        if (vm.selectedStore != null) {
            Button(
                onClick = { vm.refreshPrices() },
                enabled = vm.priceStatus != PriceStatus.LOADING,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (vm.priceStatus == PriceStatus.LOADING) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Text("  Consultando precios…")
                } else {
                    Icon(Icons.Filled.Refresh, contentDescription = null)
                    Text("  Actualizar precios")
                }
            }
            StatusText(vm)
        } else {
            Text("Precios estimados de mercado. Elige una tienda para traer precios reales en línea.",
                style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
        }
    }
}

@Composable
private fun StatusText(vm: AppViewModel) {
    val name = vm.selectedStore?.name ?: ""
    when (vm.priceStatus) {
        PriceStatus.UPDATED -> Text(
            "Precios reales de $name. Debajo de cada ingrediente ves el producto usado.",
            style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary
        )
        PriceStatus.FAILED -> Text(
            "No se pudieron traer los precios (¿sin internet?). Se muestran los estimados.",
            style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error
        )
        PriceStatus.IDLE -> Text(
            "Toca «Actualizar precios» para traer los precios reales de $name.",
            style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline
        )
        PriceStatus.LOADING -> {}
    }
}

@Composable
private fun TotalCard(vm: AppViewModel, total: Double) {
    Card(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Column(Modifier.padding(16.dp)) {
            Text(
                if (vm.priceStatus == PriceStatus.UPDATED) "Total real estimado" else "Total estimado",
                style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.outline
            )
            Text(gtq(total), style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            val ts = vm.lastPriceUpdate
            if (ts != null && vm.priceStatus == PriceStatus.UPDATED) {
                val fmt = SimpleDateFormat("d MMM, HH:mm", Locale("es"))
                Text("Actualizado ${fmt.format(Date(ts))}",
                    style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
            }
        }
    }
}

@Composable
private fun ShoppingRow(
    item: ShoppingItem,
    storeSelected: Boolean,
    updated: Boolean,
    checked: Boolean,
    onToggle: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable { onToggle() }.padding(vertical = 8.dp),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.Top, modifier = Modifier.weight(1f)) {
            Icon(
                if (checked) Icons.Filled.CheckCircle else Icons.Outlined.Circle,
                contentDescription = null,
                tint = if (checked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
            )
            Column(Modifier.padding(start = 10.dp)) {
                Text(
                    text = item.name + if (item.occurrences > 1) "  ×${item.occurrences}" else "",
                    textDecoration = if (checked) TextDecoration.LineThrough else TextDecoration.None,
                    color = if (checked) MaterialTheme.colorScheme.outline else MaterialTheme.colorScheme.onSurface
                )
                val product = item.liveProductName
                if (product != null) {
                    Text(product, style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline, maxLines = 1)
                } else if (storeSelected && updated) {
                    Text("sin match · precio estimado",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.tertiary)
                }
            }
        }
        Text(
            gtq(item.effectiveCost),
            color = if (item.isLive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
        )
    }
}
