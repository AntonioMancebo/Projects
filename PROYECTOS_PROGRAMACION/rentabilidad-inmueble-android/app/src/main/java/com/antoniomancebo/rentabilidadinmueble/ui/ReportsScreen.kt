package com.antoniomancebo.rentabilidadinmueble.ui

import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.antoniomancebo.rentabilidadinmueble.storage.SavedPropertyReport
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ReportsScreen(
    reports: List<SavedPropertyReport>,
    onDelete: (Long) -> Unit
) {
    val expanded = remember { mutableStateMapOf<Long, Boolean>() }

    if (reports.isEmpty()) {
        Column(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("Todavía no hay informes guardados.", style = MaterialTheme.typography.titleMedium)
            Text("Guarda una operación desde la pestaña Calculadora y aparecerá aquí.")
        }
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text("Informes guardados", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text("Cada informe conserva los datos y resultados existentes en el momento de guardarlo.")
        }

        items(reports, key = { it.id }) { report ->
            ReportCard(
                report = report,
                expanded = expanded[report.id] == true,
                onToggle = { expanded[report.id] = !(expanded[report.id] == true) },
                onDelete = { onDelete(report.id) }
            )
        }
    }
}

@Composable
private fun ReportCard(
    report: SavedPropertyReport,
    expanded: Boolean,
    onToggle: () -> Unit,
    onDelete: () -> Unit
) {
    val context = LocalContext.current
    val mortgage1 = report.mortgages.firstOrNull()

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(report.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(formatDate(report.createdAt), style = MaterialTheme.typography.bodySmall)
            ReportRow("Precio compra", moneyReport(report.inputs.purchasePrice))
            ReportRow("Reforma", moneyReport(report.inputs.reforms))
            ReportRow("Alquiler", moneyReport(report.inputs.monthlyRent) + "/mes")
            ReportRow("Rentabilidad neta DI", percentReport(report.result.netYieldDiPercent), true)
            ReportRow("Cash-on-Cash", percentReport(report.result.cashOnCashPercent), true)
            ReportRow("Cashflow DI", moneyReport(report.result.cashFlowDiMonthly) + "/mes")
            if (mortgage1 != null) ReportRow("Cuota hipoteca 1", moneyReport(mortgage1.monthlyPayment) + "/mes")

            OutlinedButton(onClick = onToggle, modifier = Modifier.fillMaxWidth()) {
                Text(if (expanded) "Ocultar detalle" else "Ver informe completo")
            }

            if (expanded) {
                HorizontalDivider()
                Text("Compra", fontWeight = FontWeight.Bold)
                ReportRow("Entrada", percentReport(report.inputs.downPaymentPercent))
                ReportRow("Comisión agencia", moneyReport(report.inputs.agencyCommission))
                ReportRow("Coste total", moneyReport(report.result.acquisitionCost))
                ReportRow("Cash total + reforma", moneyReport(report.result.cashNeededIncludingReforms))

                Text("Gastos y fiscalidad", fontWeight = FontWeight.Bold)
                ReportRow("Seguro impago", moneyReport(report.inputs.rentDefaultInsuranceAnnual) + "/año")
                ReportRow("Basuras", moneyReport(report.inputs.wasteTaxAnnual) + "/año")
                ReportRow("Seguro hogar", moneyReport(report.inputs.homeInsuranceAnnual) + "/año")
                ReportRow("Seguro vida", moneyReport(report.inputs.lifeInsuranceAnnual) + "/año")
                ReportRow("Comunidad", moneyReport(report.inputs.communityAnnual) + "/año")
                ReportRow("IBI", moneyReport(report.inputs.ibiAnnual) + "/año")
                ReportRow("IRPF marginal", percentReport(report.inputs.incomeTaxPercent))
                ReportRow("Beneficio neto DI", moneyReport(report.result.netBenefitDi) + "/año")
                ReportRow("ROCE", percentReport(report.result.rocePercent))

                report.scenarios.zip(report.mortgages).forEachIndexed { index, (scenario, mortgage) ->
                    Text("Hipoteca ${index + 1}", fontWeight = FontWeight.Bold)
                    ReportRow("Años", scenario.years.toString())
                    ReportRow("TIN", percentReport(scenario.tinPercent))
                    ReportRow("Capital financiado", moneyReport(mortgage.principal))
                    ReportRow("Intereses totales", moneyReport(mortgage.totalInterest))
                    ReportRow("Cuota mensual", moneyReport(mortgage.monthlyPayment))
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            val send = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_SUBJECT, report.name)
                                putExtra(Intent.EXTRA_TEXT, reportAsText(report))
                            }
                            context.startActivity(Intent.createChooser(send, "Compartir informe"))
                        },
                        modifier = Modifier.weight(1f)
                    ) { Text("Compartir") }

                    OutlinedButton(onClick = onDelete, modifier = Modifier.weight(1f)) {
                        Text("Eliminar")
                    }
                }
            }
        }
    }
}

@Composable
private fun ReportRow(label: String, value: String, bold: Boolean = false) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, modifier = Modifier.weight(1f))
        Text(value, fontWeight = if (bold) FontWeight.Bold else FontWeight.Medium)
    }
}

private fun reportAsText(r: SavedPropertyReport): String = buildString {
    appendLine(r.name)
    appendLine("Precio compra: ${moneyReport(r.inputs.purchasePrice)}")
    appendLine("Entrada: ${percentReport(r.inputs.downPaymentPercent)}")
    appendLine("Reforma: ${moneyReport(r.inputs.reforms)}")
    appendLine("Alquiler: ${moneyReport(r.inputs.monthlyRent)}/mes")
    appendLine("Coste total: ${moneyReport(r.result.acquisitionCost)}")
    appendLine("Rentabilidad neta DI: ${percentReport(r.result.netYieldDiPercent)}")
    appendLine("Cash-on-Cash: ${percentReport(r.result.cashOnCashPercent)}")
    appendLine("Cashflow DI: ${moneyReport(r.result.cashFlowDiMonthly)}/mes")
    r.scenarios.zip(r.mortgages).forEachIndexed { index, pair ->
        appendLine("Hipoteca ${index + 1}: ${pair.first.years} años · TIN ${percentReport(pair.first.tinPercent)} · cuota ${moneyReport(pair.second.monthlyPayment)}/mes")
    }
}

private fun formatDate(epoch: Long): String =
    SimpleDateFormat("dd/MM/yyyy HH:mm", Locale("es", "ES")).format(Date(epoch))

private fun moneyReport(value: Double): String =
    NumberFormat.getCurrencyInstance(Locale("es", "ES")).format(value)

private fun percentReport(value: Double): String =
    NumberFormat.getNumberInstance(Locale("es", "ES")).apply {
        minimumFractionDigits = 2
        maximumFractionDigits = 2
    }.format(value) + " %"
