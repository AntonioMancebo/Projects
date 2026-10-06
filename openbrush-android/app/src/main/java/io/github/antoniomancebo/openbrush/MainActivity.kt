package io.github.antoniomancebo.openbrush

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import io.github.antoniomancebo.openbrush.ble.BrushPressure
import io.github.antoniomancebo.openbrush.data.BrushSession
import io.github.antoniomancebo.openbrush.ui.theme.OpenBrushTheme
import java.text.DateFormat
import java.util.Date
import kotlin.math.min

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { OpenBrushTheme { OpenBrushApp() } }
    }
}

@Composable
private fun OpenBrushApp(vm: OpenBrushViewModel = viewModel()) {
    val state by vm.uiState.collectAsState()
    val context = LocalContext.current

    val permissions = if (Build.VERSION.SDK_INT >= 31) {
        arrayOf(Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT)
    } else {
        arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
    }

    fun hasPermissions(): Boolean = permissions.all {
        context.checkSelfPermission(it) == PackageManager.PERMISSION_GRANTED
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        if (result.values.all { it }) vm.startScanning()
    }

    LaunchedEffect(Unit) {
        if (hasPermissions()) vm.startScanning()
    }

    Scaffold { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                Text("OpenBrush", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
                Text(
                    "Cepillado local por Bluetooth · sin cuenta · sin permiso de Internet",
                    style = MaterialTheme.typography.bodyMedium,
                )
            }

            item {
                val ad = state.lastAdvertisement
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Cepillo", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                        if (ad == null) {
                            Text(if (state.scanning) "Buscando un Oral-B cercano…" else "Escaneo detenido")
                        } else {
                            Text(ad.modelName, fontWeight = FontWeight.Bold)
                            Text("Estado: ${ad.state.label}")
                            Text("Modo: ${ad.modeName}")
                            Text("Tiempo: ${formatDuration(ad.elapsedSeconds)}")
                            Text("Sector: ${ad.sector?.let { "$it/${ad.numberOfSectors ?: 4}" } ?: "—"}")
                            Text(
                                "Presión: ${ad.pressure.label}",
                                fontWeight = if (ad.pressure == BrushPressure.HIGH) FontWeight.Bold else FontWeight.Normal,
                            )
                            Text("Señal: ${ad.rssi} dBm", style = MaterialTheme.typography.bodySmall)
                            LinearProgressIndicator(
                                progress = { min(ad.elapsedSeconds / 120f, 1f) },
                                modifier = Modifier.fillMaxWidth(),
                            )
                            Text(
                                if (ad.elapsedSeconds >= 120) "Objetivo de 2 minutos completado"
                                else "${120 - ad.elapsedSeconds}s para llegar a 2 minutos",
                                style = MaterialTheme.typography.bodySmall,
                            )
                        }
                    }
                }
            }

            state.error?.let { error ->
                item { Card(modifier = Modifier.fillMaxWidth()) { Text(error, modifier = Modifier.padding(16.dp)) } }
            }

            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        modifier = Modifier.weight(1f),
                        onClick = {
                            if (state.scanning) vm.stopScanning()
                            else if (hasPermissions()) vm.startScanning()
                            else permissionLauncher.launch(permissions)
                        },
                    ) { Text(if (state.scanning) "Detener" else "Escanear") }
                    OutlinedButton(
                        modifier = Modifier.weight(1f),
                        onClick = vm::clearHistory,
                        enabled = state.sessions.isNotEmpty(),
                    ) { Text("Borrar historial") }
                }
            }

            item {
                Spacer(Modifier.height(4.dp))
                Text("Historial local", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text(
                    "v0.1 guarda automáticamente las sesiones detectadas de al menos 10 segundos.",
                    style = MaterialTheme.typography.bodySmall,
                )
            }

            if (state.sessions.isEmpty()) {
                item { Text("Todavía no hay cepillados guardados.") }
            } else {
                items(state.sessions, key = { it.startedAtEpochMs }) { session -> SessionRow(session) }
            }
        }
    }
}

@Composable
private fun SessionRow(session: BrushSession) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).format(Date(session.startedAtEpochMs)),
                    fontWeight = FontWeight.SemiBold,
                )
                Text(session.mode, style = MaterialTheme.typography.bodySmall)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(formatDuration(session.durationSeconds), fontWeight = FontWeight.Bold)
                Text(
                    if (session.highPressureObserved) "Presión alta detectada" else "Presión correcta",
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }
        HorizontalDivider(modifier = Modifier.padding(top = 10.dp))
    }
}

private fun formatDuration(seconds: Int): String = "%d:%02d".format(seconds / 60, seconds % 60)
