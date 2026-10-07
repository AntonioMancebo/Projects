package com.antoniomancebo.rentabilidadinmueble.ui

import android.net.Uri
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import com.antoniomancebo.rentabilidadinmueble.storage.AppRepository

data class SharedProductDraft(
    val url: String,
    val title: String = ""
)

@Composable
fun RentabilityApp(
    incomingShare: SharedProductDraft?,
    onShareConsumed: () -> Unit
) {
    val context = LocalContext.current
    val repository = remember { AppRepository(context.applicationContext) }
    var section by rememberSaveable { mutableIntStateOf(if (incomingShare != null) 2 else 0) }
    var dataVersion by remember { mutableIntStateOf(0) }

    LaunchedEffect(incomingShare) {
        if (incomingShare != null) section = 2
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
    ) {
        Text(
            text = "Rentabilidad Inmueble",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
        )

        TabRow(selectedTabIndex = section) {
            listOf("Calculadora", "Informes", "Compras").forEachIndexed { index, title ->
                Tab(
                    selected = section == index,
                    onClick = { section = index },
                    text = { Text(title, maxLines = 1) }
                )
            }
        }

        Box(modifier = Modifier.weight(1f)) {
            when (section) {
                0 -> CalculatorScreen(
                    onSaveReport = { name, inputs, scenarios, mortgages, result ->
                        repository.saveReport(name, inputs, scenarios, mortgages, result)
                        dataVersion++
                    }
                )
                1 -> ReportsScreen(
                    reports = remember(dataVersion) { repository.loadReports() },
                    onDelete = {
                        repository.deleteReport(it)
                        dataVersion++
                    }
                )
                else -> ShoppingScreen(
                    items = remember(dataVersion) { repository.loadShoppingItems() },
                    incomingShare = incomingShare,
                    onShareConsumed = onShareConsumed,
                    onSave = { title, category, url, price, shipping, notes ->
                        repository.saveShoppingItem(title, category, url, price, shipping, notes)
                        dataVersion++
                    },
                    onDelete = {
                        repository.deleteShoppingItem(it)
                        dataVersion++
                    }
                )
            }
        }
    }
}

fun titleFromUrl(url: String): String {
    val host = runCatching { Uri.parse(url).host.orEmpty() }.getOrDefault("")
        .removePrefix("www.")
    return host.ifBlank { "Artículo" }
}
