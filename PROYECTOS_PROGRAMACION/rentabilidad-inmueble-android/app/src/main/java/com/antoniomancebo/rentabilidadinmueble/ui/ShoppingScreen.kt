package com.antoniomancebo.rentabilidadinmueble.ui

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.antoniomancebo.rentabilidadinmueble.storage.ShoppingItem
import java.text.NumberFormat
import java.util.Locale

private val categories = listOf("Cocina", "Salón", "Dormitorio", "Baño", "Exterior", "General")

private enum class ShoppingSort(val label: String) {
    TOTAL("Total"),
    ITEM("Artículo"),
    SHIPPING("Envío")
}

@Composable
fun ShoppingScreen(
    items: List<ShoppingItem>,
    incomingShare: SharedProductDraft?,
    onShareConsumed: () -> Unit,
    onSave: (String, String, String, Double, Double, String) -> Unit,
    onDelete: (Long) -> Unit
) {
    var showForm by rememberSaveable { mutableStateOf(false) }
    var title by rememberSaveable { mutableStateOf("") }
    var category by rememberSaveable { mutableStateOf("Cocina") }
    var url by rememberSaveable { mutableStateOf("") }
    var itemPrice by rememberSaveable { mutableStateOf("") }
    var shippingPrice by rememberSaveable { mutableStateOf("0") }
    var notes by rememberSaveable { mutableStateOf("") }
    var sort by remember { mutableStateOf(ShoppingSort.TOTAL) }

    LaunchedEffect(incomingShare) {
        incomingShare?.let {
            showForm = true
            url = it.url
            title = it.title.ifBlank { titleFromUrl(it.url) }
            onShareConsumed()
        }
    }

    val sorted = when (sort) {
        ShoppingSort.TOTAL -> items.sortedBy { it.totalPrice }
        ShoppingSort.ITEM -> items.sortedBy { it.itemPrice }
        ShoppingSort.SHIPPING -> items.sortedBy { it.shippingPrice }
    }

    androidx.compose.foundation.lazy.LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text("Lista de compras", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text("Guarda ofertas por estancia y compáralas por precio real, incluyendo el envío.")
        }

        item {
            Button(onClick = { showForm = !showForm }, modifier = Modifier.fillMaxWidth()) {
                Text(if (showForm) "Cerrar formulario" else "Añadir artículo")
            }
        }

        if (showForm) {
            item {
                ShoppingForm(
                    title = title,
                    onTitle = { title = it },
                    category = category,
                    onCategory = { category = it },
                    url = url,
                    onUrl = { url = it },
                    itemPrice = itemPrice,
                    onItemPrice = { itemPrice = it },
                    shippingPrice = shippingPrice,
                    onShippingPrice = { shippingPrice = it },
                    notes = notes,
                    onNotes = { notes = it },
                    onSave = {
                        if (url.isNotBlank() && itemPrice.toPrice() >= 0.0) {
                            onSave(
                                title.ifBlank { titleFromUrl(url) },
                                category,
                                url,
                                itemPrice.toPrice(),
                                shippingPrice.toPrice(),
                                notes
                            )
                            title = ""
                            url = ""
                            itemPrice = ""
                            shippingPrice = "0"
                            notes = ""
                            showForm = false
                        }
                    }
                )
            }
        }

        if (items.isNotEmpty()) {
            item {
                Text("Ordenar por", fontWeight = FontWeight.Bold)
                Row(
                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ShoppingSort.entries.forEach { option ->
                        FilterChip(
                            selected = sort == option,
                            onClick = { sort = option },
                            label = { Text(option.label) }
                        )
                    }
                }
            }
        }

        if (items.isEmpty()) {
            item {
                Text("Aún no hay artículos. También puedes compartir un enlace desde el navegador directamente a esta app.")
            }
        } else {
            categories.forEach { group ->
                val groupItems = sorted.filter { it.category == group }
                if (groupItems.isNotEmpty()) {
                    item { Text(group, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold) }
                    items(groupItems, key = { it.id }) { item ->
                        ShoppingItemCard(item, onDelete)
                    }
                }
            }
            val other = sorted.filter { it.category !in categories }
            if (other.isNotEmpty()) {
                item { Text("Otros", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold) }
                items(other, key = { it.id }) { item ->
                    ShoppingItemCard(item, onDelete)
                }
            }
        }
    }
}

@Composable
private fun ShoppingForm(
    title: String,
    onTitle: (String) -> Unit,
    category: String,
    onCategory: (String) -> Unit,
    url: String,
    onUrl: (String) -> Unit,
    itemPrice: String,
    onItemPrice: (String) -> Unit,
    shippingPrice: String,
    onShippingPrice: (String) -> Unit,
    notes: String,
    onNotes: (String) -> Unit,
    onSave: () -> Unit
) {
    Card(shape = RoundedCornerShape(18.dp), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("Nuevo artículo", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            OutlinedTextField(title, onTitle, label = { Text("Nombre / descripción") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(url, onUrl, label = { Text("Enlace") }, modifier = Modifier.fillMaxWidth())

            Text("Clasificar en")
            Row(
                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                categories.forEach { option ->
                    FilterChip(
                        selected = category == option,
                        onClick = { onCategory(option) },
                        label = { Text(option) }
                    )
                }
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = itemPrice,
                    onValueChange = { onItemPrice(it.onlyPriceChars()) },
                    label = { Text("Precio artículo") },
                    suffix = { Text("€") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = shippingPrice,
                    onValueChange = { onShippingPrice(it.onlyPriceChars()) },
                    label = { Text("Envío") },
                    suffix = { Text("€") },
                    supportingText = { Text("0 = gratis") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.weight(1f)
                )
            }

            OutlinedTextField(
                value = notes,
                onValueChange = onNotes,
                label = { Text("Notas opcionales") },
                modifier = Modifier.fillMaxWidth()
            )
            Text("Coste total: " + shoppingMoney(itemPrice.toPrice() + shippingPrice.toPrice()), fontWeight = FontWeight.Bold)
            Button(onClick = onSave, enabled = url.isNotBlank() && itemPrice.isNotBlank(), modifier = Modifier.fillMaxWidth()) {
                Text("Guardar artículo")
            }
        }
    }
}

@Composable
private fun ShoppingItemCard(item: ShoppingItem, onDelete: (Long) -> Unit) {
    val context = LocalContext.current
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(item.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(item.category, style = MaterialTheme.typography.bodySmall)
            ShoppingRow("Artículo", shoppingMoney(item.itemPrice))
            ShoppingRow("Envío", if (item.shippingPrice == 0.0) "Gratis" else shoppingMoney(item.shippingPrice))
            ShoppingRow("TOTAL", shoppingMoney(item.totalPrice), true)
            if (item.notes.isNotBlank()) Text(item.notes, style = MaterialTheme.typography.bodySmall)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = {
                        runCatching {
                            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(item.url)))
                        }
                    },
                    modifier = Modifier.weight(1f)
                ) { Text("Abrir enlace") }
                OutlinedButton(onClick = { onDelete(item.id) }, modifier = Modifier.weight(1f)) {
                    Text("Eliminar")
                }
            }
        }
    }
}

@Composable
private fun ShoppingRow(label: String, value: String, bold: Boolean = false) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label)
        Text(value, fontWeight = if (bold) FontWeight.Bold else FontWeight.Medium)
    }
}

private fun String.onlyPriceChars(): String = filter { it.isDigit() || it == ',' || it == '.' }
private fun String.toPrice(): Double = replace(",", ".").toDoubleOrNull() ?: 0.0
private fun shoppingMoney(value: Double): String =
    NumberFormat.getCurrencyInstance(Locale("es", "ES")).format(value)
