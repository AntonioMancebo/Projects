package com.antoniomancebo.rentabilidadinmueble.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.antoniomancebo.rentabilidadinmueble.calculator.InvestmentCalculator
import com.antoniomancebo.rentabilidadinmueble.calculator.InvestmentInputs
import com.antoniomancebo.rentabilidadinmueble.calculator.MortgageResult
import com.antoniomancebo.rentabilidadinmueble.calculator.MortgageScenario
import java.text.NumberFormat
import java.util.Locale

private val editableYellow = Color(0xFFFFF2B2)
private val softRed = Color(0xFFFFD9D9)
private val softAmber = Color(0xFFFFEDB5)
private val softGreen = Color(0xFFD8F3DC)

@Composable
fun CalculatorScreen() {
    var purchase by remember { mutableStateOf("45000") }
    var downPayment by remember { mutableStateOf("10") }
    var reforms by remember { mutableStateOf("0") }
    var agency by remember { mutableStateOf("0") }
    var rent by remember { mutableStateOf("0") }

    var years1 by remember { mutableStateOf("30") }
    var tin1 by remember { mutableStateOf("2.44") }
    var years2 by remember { mutableStateOf("30") }
    var tin2 by remember { mutableStateOf("1.55") }
    var years3 by remember { mutableStateOf("30") }
    var tin3 by remember { mutableStateOf("1.55") }

    val inputs = InvestmentInputs(
        purchasePrice = purchase.toNumber(),
        downPaymentPercent = downPayment.toNumber(),
        reforms = reforms.toNumber(),
        agencyCommission = agency.toNumber(),
        monthlyRent = rent.toNumber()
    )

    val scenarios = listOf(
        MortgageScenario("HIPOTECA FIJA 1", years1.toIntSafe(30), tin1.toNumber()),
        MortgageScenario("HIPOTECA FIJA 2", years2.toIntSafe(30), tin2.toNumber()),
        MortgageScenario("HIPOTECA FIJA 3", years3.toIntSafe(30), tin3.toNumber())
    )

    val mortgageResults = scenarios.map { InvestmentCalculator.mortgage(inputs, it) }
    val primaryInvestment = InvestmentCalculator.investment(inputs, mortgageResults.first())

    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = "Rentabilidad de inmueble",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Calculadora móvil basada en tu Excel. Los campos amarillos son editables.",
                style = MaterialTheme.typography.bodyMedium
            )

            SectionCard("DATOS DEL INMUEBLE") {
                EditableNumber("Coste compra (€)", purchase) { purchase = it }
                EditableNumber("Entrada (%)", downPayment) { downPayment = it }
                EditableNumber("Reformas / arreglos (€)", reforms) { reforms = it }
                EditableNumber("Comisión agencia (€)", agency) { agency = it }
                EditableNumber("Alquiler mes (€)", rent) { rent = it }
            }

            SectionCard("RESULTADOS") {
                ValueRow("Coste total adquisición", money(primaryInvestment.acquisitionCost))
                ValueRow("Dinero aportado", money(primaryInvestment.cashInvested))
                ValueRow("Alquiler anual", money(primaryInvestment.annualRent))
                HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp))

                PercentCard(
                    label = "Rentabilidad Neta (DI)",
                    value = primaryInvestment.netYieldDiPercent,
                    provisional = true
                )
                Spacer(Modifier.height(8.dp))
                PercentCard(
                    label = "Cash-on-Cash Return",
                    value = primaryInvestment.cashOnCashPercent,
                    provisional = true
                )
                Text(
                    text = "Las dos rentabilidades quedan aisladas para sustituirlas por las fórmulas exactas del Excel original.",
                    style = MaterialTheme.typography.bodySmall
                )
            }

            MortgageCard(
                scenario = scenarios[0],
                result = mortgageResults[0],
                years = years1,
                tin = tin1,
                onYears = { years1 = it },
                onTin = { tin1 = it }
            )
            MortgageCard(
                scenario = scenarios[1],
                result = mortgageResults[1],
                years = years2,
                tin = tin2,
                onYears = { years2 = it },
                onTin = { tin2 = it }
            )
            MortgageCard(
                scenario = scenarios[2],
                result = mortgageResults[2],
                years = years3,
                tin = tin3,
                onYears = { years3 = it },
                onTin = { tin3 = it }
            )

            Text(
                text = "Semáforo provisional: rojo < 5 %, amarillo 5–8 %, verde ≥ 8 %. Se ajustará a los límites exactos del Excel.",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
private fun SectionCard(title: String, content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(title, fontWeight = FontWeight.Bold)
            content()
        }
    }
}

@Composable
private fun EditableNumber(label: String, value: String, onValueChange: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = { raw ->
            onValueChange(raw.filter { it.isDigit() || it == '.' || it == ',' || it == '-' })
        },
        label = { Text(label) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        singleLine = true,
        modifier = Modifier
            .fillMaxWidth()
            .background(editableYellow, RoundedCornerShape(12.dp))
    )
}

@Composable
private fun MortgageCard(
    scenario: MortgageScenario,
    result: MortgageResult,
    years: String,
    tin: String,
    onYears: (String) -> Unit,
    onTin: (String) -> Unit
) {
    SectionCard("DATOS " + scenario.title) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = years,
                onValueChange = { onYears(it.filter(Char::isDigit)) },
                label = { Text("Años") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = tin,
                onValueChange = { onTin(it.filter { c -> c.isDigit() || c == '.' || c == ',' }) },
                label = { Text("TIN (%)") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.weight(1f)
            )
        }

        ValueRow("Financiación", percent(result.financingPercent))
        ValueRow("Capital financiado · Total", money(result.principal))
        ValueRow("Intereses · Total", money(result.totalInterest))
        HorizontalDivider()
        ValueRow("Capital · Anual medio", money(result.averagePrincipalPerYear))
        ValueRow("Intereses · Anual medio", money(result.averageInterestPerYear))
        ValueRow("Capital · Mensual medio", money(result.averagePrincipalPerMonth))
        ValueRow("Intereses · Mensual medio", money(result.averageInterestPerMonth))
        HorizontalDivider()
        ValueRow("Cuota mensual", money(result.monthlyPayment), bold = true)
    }
}

@Composable
private fun ValueRow(label: String, value: String, bold: Boolean = false) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, modifier = Modifier.weight(1f))
        Text(value, fontWeight = if (bold) FontWeight.Bold else FontWeight.Medium)
    }
}

@Composable
private fun PercentCard(label: String, value: Double, provisional: Boolean) {
    val bg = when {
        value < 5.0 -> softRed
        value < 8.0 -> softAmber
        else -> softGreen
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(bg, RoundedCornerShape(14.dp))
            .padding(14.dp)
    ) {
        Text(label, fontWeight = FontWeight.Bold)
        Text(percent(value), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        if (provisional) {
            Text("Fórmula provisional hasta leer el Excel", style = MaterialTheme.typography.bodySmall)
        }
    }
}

private fun String.toNumber(): Double =
    replace(",", ".").toDoubleOrNull() ?: 0.0

private fun String.toIntSafe(default: Int): Int =
    toIntOrNull()?.coerceAtLeast(1) ?: default

private fun money(value: Double): String =
    NumberFormat.getCurrencyInstance(Locale("es", "ES")).format(value)

private fun percent(value: Double): String =
    NumberFormat.getNumberInstance(Locale("es", "ES")).apply {
        minimumFractionDigits = 2
        maximumFractionDigits = 2
    }.format(value) + " %"
