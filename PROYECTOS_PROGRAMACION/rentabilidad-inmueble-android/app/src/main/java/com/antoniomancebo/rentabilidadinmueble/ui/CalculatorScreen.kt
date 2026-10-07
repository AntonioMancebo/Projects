package com.antoniomancebo.rentabilidadinmueble.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.antoniomancebo.rentabilidadinmueble.calculator.InvestmentCalculator
import com.antoniomancebo.rentabilidadinmueble.calculator.InvestmentInputs
import com.antoniomancebo.rentabilidadinmueble.calculator.InvestmentResult
import com.antoniomancebo.rentabilidadinmueble.calculator.MortgageResult
import com.antoniomancebo.rentabilidadinmueble.calculator.MortgageScenario
import java.text.NumberFormat
import java.util.Locale

private val editableYellow = Color(0xFFFFF4A8)
private val softRed = Color(0xFFFFD9D9)
private val softAmber = Color(0xFFFFEDB5)
private val softGreen = Color(0xFFD8F3DC)
private val softBlue = Color(0xFFDDEBFF)

@Composable
fun CalculatorScreen(
    onSaveReport: (
        String,
        InvestmentInputs,
        List<MortgageScenario>,
        List<MortgageResult>,
        InvestmentResult
    ) -> Unit = { _, _, _, _, _ -> }
) {
    var selectedTab by rememberSaveable { mutableStateOf(0) }
    var reportName by rememberSaveable { mutableStateOf("") }
    var saveMessage by rememberSaveable { mutableStateOf("") }
    var showSaveDialog by rememberSaveable { mutableStateOf(false) }

    var purchase by rememberSaveable { mutableStateOf("60000") }
    var downPayment by rememberSaveable { mutableStateOf("10") }
    var reforms by rememberSaveable { mutableStateOf("1000") }
    var agency by rememberSaveable { mutableStateOf("0") }
    var rent by rememberSaveable { mutableStateOf("490") }

    var rentDefaultInsurance by rememberSaveable { mutableStateOf("0") }
    var wasteTax by rememberSaveable { mutableStateOf("64") }
    var homeInsurance by rememberSaveable { mutableStateOf("200") }
    var lifeInsurance by rememberSaveable { mutableStateOf("0") }
    var community by rememberSaveable { mutableStateOf("250") }
    var ibi by rememberSaveable { mutableStateOf("175.63") }

    var incomeTaxPercent by rememberSaveable { mutableStateOf("30") }
    var constructionValuePercent by rememberSaveable { mutableStateOf("30") }

    var years1 by rememberSaveable { mutableStateOf("30") }
    var tin1 by rememberSaveable { mutableStateOf("2.44") }
    var years2 by rememberSaveable { mutableStateOf("30") }
    var tin2 by rememberSaveable { mutableStateOf("1.55") }
    var years3 by rememberSaveable { mutableStateOf("30") }
    var tin3 by rememberSaveable { mutableStateOf("1.55") }

    val inputs = remember(
        purchase, downPayment, reforms, agency, rent,
        rentDefaultInsurance, wasteTax, homeInsurance, lifeInsurance, community, ibi,
        incomeTaxPercent, constructionValuePercent
    ) {
        InvestmentInputs(
            purchasePrice = purchase.toNumber(),
            downPaymentPercent = downPayment.toNumber(),
            reforms = reforms.toNumber(),
            agencyCommission = agency.toNumber(),
            monthlyRent = rent.toNumber(),
            rentDefaultInsuranceAnnual = rentDefaultInsurance.toNumber(),
            wasteTaxAnnual = wasteTax.toNumber(),
            homeInsuranceAnnual = homeInsurance.toNumber(),
            lifeInsuranceAnnual = lifeInsurance.toNumber(),
            communityAnnual = community.toNumber(),
            ibiAnnual = ibi.toNumber(),
            incomeTaxPercent = incomeTaxPercent.toNumber(),
            constructionValuePercent = constructionValuePercent.toNumber()
        )
    }

    val scenarios = listOf(
        MortgageScenario("Hipoteca fija 1", years1.toIntSafe(30), tin1.toNumber()),
        MortgageScenario("Hipoteca fija 2", years2.toIntSafe(30), tin2.toNumber()),
        MortgageScenario("Hipoteca fija 3", years3.toIntSafe(30), tin3.toNumber())
    )

    val mortgageResults = scenarios.map { InvestmentCalculator.mortgage(inputs, it) }
    val result = InvestmentCalculator.investment(inputs, mortgageResults.first())

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Rentabilidad de inmueble",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Hipoteca 1 = escenario principal",
                    style = MaterialTheme.typography.bodySmall
                )
            }
            OutlinedButton(
                onClick = {
                    saveMessage = ""
                    showSaveDialog = true
                }
            ) {
                Text("Guardar")
            }
        }

        if (saveMessage.isNotBlank()) {
            Text(
                text = saveMessage,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 2.dp)
            )
        }

        TabRow(selectedTabIndex = selectedTab) {
            listOf("Resumen", "Gastos", "Hipotecas").forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = { Text(title, maxLines = 1) }
                )
            }
        }

        when (selectedTab) {
            0 -> SummaryTab(
                purchase = purchase,
                onPurchase = { purchase = it },
                downPayment = downPayment,
                onDownPayment = { downPayment = it },
                reforms = reforms,
                onReforms = { reforms = it },
                agency = agency,
                onAgency = { agency = it },
                rent = rent,
                onRent = { rent = it },
                result = result
            )

            1 -> ExpensesTab(
                rentDefaultInsurance = rentDefaultInsurance,
                onRentDefaultInsurance = { rentDefaultInsurance = it },
                wasteTax = wasteTax,
                onWasteTax = { wasteTax = it },
                homeInsurance = homeInsurance,
                onHomeInsurance = { homeInsurance = it },
                lifeInsurance = lifeInsurance,
                onLifeInsurance = { lifeInsurance = it },
                community = community,
                onCommunity = { community = it },
                ibi = ibi,
                onIbi = { ibi = it },
                incomeTaxPercent = incomeTaxPercent,
                onIncomeTaxPercent = { incomeTaxPercent = it },
                constructionValuePercent = constructionValuePercent,
                onConstructionValuePercent = { constructionValuePercent = it },
                result = result
            )

            else -> MortgagesTab(
                scenarios = scenarios,
                results = mortgageResults,
                years1 = years1,
                tin1 = tin1,
                years2 = years2,
                tin2 = tin2,
                years3 = years3,
                tin3 = tin3,
                onYears1 = { years1 = it },
                onTin1 = { tin1 = it },
                onYears2 = { years2 = it },
                onTin2 = { tin2 = it },
                onYears3 = { years3 = it },
                onTin3 = { tin3 = it }
            )
        }
    }

    if (showSaveDialog) {
        AlertDialog(
            onDismissRequest = {
                showSaveDialog = false
                reportName = ""
            },
            title = { Text("Guardar informe") },
            text = {
                OutlinedTextField(
                    value = reportName,
                    onValueChange = { reportName = it },
                    label = { Text("Nombre del informe (opcional)") },
                    placeholder = { Text("Ej.: Piso Jaén centro") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onSaveReport(reportName, inputs, scenarios, mortgageResults, result)
                        reportName = ""
                        saveMessage = "✓ Informe guardado"
                        showSaveDialog = false
                    }
                ) {
                    Text("Guardar")
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = {
                        reportName = ""
                        showSaveDialog = false
                    }
                ) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
private fun SummaryTab(
    purchase: String,
    onPurchase: (String) -> Unit,
    downPayment: String,
    onDownPayment: (String) -> Unit,
    reforms: String,
    onReforms: (String) -> Unit,
    agency: String,
    onAgency: (String) -> Unit,
    rent: String,
    onRent: (String) -> Unit,
    result: InvestmentResult
) {
    ScrollColumn {
        SectionCard("Datos editables del inmueble") {
            EditableNumber("Entrada (%)", downPayment, onDownPayment)
            EditableNumber("Coste compra (€)", purchase, onPurchase)
            EditableNumber("Reformas / arreglos (€)", reforms, onReforms)
            EditableNumber("Comisión agencia (€)", agency, onAgency)
            EditableNumber("Alquiler mes (€)", rent, onRent)
        }

        SectionCard("Resultado rápido") {
            ProfitabilityCard(
                label = "Rentabilidad Neta (DI)",
                value = result.netYieldDiPercent,
                redBelow = 5.0,
                greenAbove = 7.0,
                detail = "Beneficio neto / coste total"
            )
            ProfitabilityCard(
                label = "Cash-on-Cash Return",
                value = result.cashOnCashPercent,
                redBelow = 5.0,
                greenAbove = 7.0,
                detail = "Cashflow AI / efectivo aportado"
            )
            ProfitabilityCard(
                label = "Rentabilidad bruta",
                value = result.grossYieldPercent,
                redBelow = 7.0,
                greenAbove = 8.0,
                detail = "Alquiler anual / coste total"
            )
        }

        SectionCard("Compra y efectivo necesario") {
            ValueRow("Entrada", money(result.downPayment))
            ValueRow("Notario, registro, tasación, gestoría (2%)", money(result.notaryRegistryValuationManagement))
            ValueRow("ITP / IVA (7%)", money(result.transferTax))
            ValueRow("Coste total", money(result.acquisitionCost), bold = true)
            HorizontalDivider()
            ValueRow("Cash necesario para la compra", money(result.cashNeededForPurchase))
            ValueRow("Cash total + reforma", money(result.cashNeededIncludingReforms), bold = true)
        }

        SectionCard("Beneficio y cashflow") {
            ValueRow("Alquiler anual", money(result.annualRent))
            ValueRow("Beneficio (AI) · anual", money(result.preTaxBenefit))
            ValueRow("Beneficio (AI) · mes", money(result.preTaxBenefitMonthly))
            ValueRow("Rentabilidad Neta (AI)", percent(result.netYieldAiPercent))
            HorizontalDivider()
            ValueRow("Beneficio Neto (DI) · anual", money(result.netBenefitDi), bold = true)
            ValueRow("Beneficio Neto (DI) · mes", money(result.netBenefitDiMonthly))
            ValueRow("CASHFLOW (AI) · anual", money(result.cashFlowAi))
            ValueRow("CASHFLOW (AI) · mes", money(result.cashFlowAiMonthly))
            ValueRow("CASHFLOW (DI) · anual", money(result.cashFlowDi), bold = true)
            ValueRow("CASHFLOW (DI) · mes", money(result.cashFlowDiMonthly))
        }

        SectionCard("Retorno sobre el efectivo") {
            ValueRow("ROCE", percent(result.rocePercent), bold = true)
            ValueRow("ROCE (años)", years(result.roceYears))
            ValueRow("Cash-on-Cash Return", percent(result.cashOnCashPercent), bold = true)
            ValueRow("COCR (años)", years(result.cashOnCashYears))
        }
    }
}

@Composable
private fun ExpensesTab(
    rentDefaultInsurance: String,
    onRentDefaultInsurance: (String) -> Unit,
    wasteTax: String,
    onWasteTax: (String) -> Unit,
    homeInsurance: String,
    onHomeInsurance: (String) -> Unit,
    lifeInsurance: String,
    onLifeInsurance: (String) -> Unit,
    community: String,
    onCommunity: (String) -> Unit,
    ibi: String,
    onIbi: (String) -> Unit,
    incomeTaxPercent: String,
    onIncomeTaxPercent: (String) -> Unit,
    constructionValuePercent: String,
    onConstructionValuePercent: (String) -> Unit,
    result: InvestmentResult
) {
    ScrollColumn {
        SectionCard("Gastos anuales") {
            Text(
                text = "En el Excel estos importes estaban escritos como valores fijos. Aquí los dejamos editables para poder reutilizar el cálculo con cualquier vivienda.",
                style = MaterialTheme.typography.bodySmall
            )
            EditableNumber("Seguro impago (€ / año)", rentDefaultInsurance, onRentDefaultInsurance)
            EditableNumber("Impuesto basuras (€ / año)", wasteTax, onWasteTax)
            EditableNumber("Seguro hogar (€ / año)", homeInsurance, onHomeInsurance)
            EditableNumber("Seguro vida (€ / año)", lifeInsurance, onLifeInsurance)
            EditableNumber("Comunidad (€ / año)", community, onCommunity)
            EditableNumber("IBI (€ / año)", ibi, onIbi)
        }

        SectionCard("Gastos calculados por el Excel") {
            ValueRow("Mantenimiento (5%)", money(-result.maintenanceAnnual))
            ValueRow("Periodos vacío (5%)", money(-result.vacancyAnnual))
            ValueRow("Intereses Hipoteca 1", money(-result.mortgageInterestAnnual))
            ValueRow("Gastos anuales totales", money(-result.totalAnnualExpenses), bold = true)
        }

        SectionCard("Fiscalidad") {
            EditableNumber("IRPF · tipo marginal (%)", incomeTaxPercent, onIncomeTaxPercent)
            EditableNumber(
                "Valor construcción sobre compraventa (%)",
                constructionValuePercent,
                onConstructionValuePercent
            )
            HorizontalDivider()
            ValueRow("Amortización anual", money(result.depreciationAnnual))
            ValueRow("Base tras deducción vivienda habitual", money(result.taxableBaseAfterHomeReduction))
            ValueRow("IRPF estimado", money(-result.incomeTax))
            ValueRow("Beneficio Neto (DI)", money(result.netBenefitDi), bold = true)
        }

        SectionCard("Constantes del Excel") {
            ValueRow("Notario / registro / tasación / gestoría", "2,00 %")
            ValueRow("ITP / IVA", "7,00 %")
            ValueRow("Mantenimiento", "5,00 %")
            ValueRow("Periodos vacío", "5,00 %")
            ValueRow("Deducción vivienda habitual", "60,00 %")
            ValueRow("Amortización fiscal", "3,00 %")
        }
    }
}

@Composable
private fun MortgagesTab(
    scenarios: List<MortgageScenario>,
    results: List<MortgageResult>,
    years1: String,
    tin1: String,
    years2: String,
    tin2: String,
    years3: String,
    tin3: String,
    onYears1: (String) -> Unit,
    onTin1: (String) -> Unit,
    onYears2: (String) -> Unit,
    onTin2: (String) -> Unit,
    onYears3: (String) -> Unit,
    onTin3: (String) -> Unit
) {
    ScrollColumn {
        Card(
            colors = CardDefaults.cardColors(containerColor = softBlue),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "La Hipoteca 1 es la que el Excel usa para calcular intereses, devolución de capital, cashflow, ROCE y Cash-on-Cash. Las 2 y 3 sirven para comparar ofertas.",
                modifier = Modifier.padding(14.dp),
                style = MaterialTheme.typography.bodyMedium
            )
        }

        MortgageCard(
            scenario = scenarios[0],
            result = results[0],
            years = years1,
            tin = tin1,
            onYears = onYears1,
            onTin = onTin1,
            primary = true
        )
        MortgageCard(
            scenario = scenarios[1],
            result = results[1],
            years = years2,
            tin = tin2,
            onYears = onYears2,
            onTin = onTin2
        )
        MortgageCard(
            scenario = scenarios[2],
            result = results[2],
            years = years3,
            tin = tin3,
            onYears = onYears3,
            onTin = onTin3
        )
    }
}

@Composable
private fun ScrollColumn(content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        content = content
    )
}

@Composable
private fun SectionCard(title: String, content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
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
    onTin: (String) -> Unit,
    primary: Boolean = false
) {
    SectionCard(if (primary) "${scenario.title} · PRINCIPAL" else scenario.title) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            EditableNumberCompact(
                label = "Años",
                value = years,
                onValueChange = { onYears(it.filter(Char::isDigit)) },
                modifier = Modifier.weight(1f),
                keyboardType = KeyboardType.Number
            )
            EditableNumberCompact(
                label = "TIN (%)",
                value = tin,
                onValueChange = onTin,
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
private fun EditableNumberCompact(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    keyboardType: KeyboardType = KeyboardType.Decimal
) {
    OutlinedTextField(
        value = value,
        onValueChange = { raw ->
            onValueChange(raw.filter { it.isDigit() || it == '.' || it == ',' || it == '-' })
        },
        label = { Text(label) },
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        singleLine = true,
        modifier = modifier.background(editableYellow, RoundedCornerShape(12.dp))
    )
}

@Composable
private fun ValueRow(label: String, value: String, bold: Boolean = false) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, modifier = Modifier.weight(1f))
        Text(
            text = value,
            fontWeight = if (bold) FontWeight.Bold else FontWeight.Medium
        )
    }
}

@Composable
private fun ProfitabilityCard(
    label: String,
    value: Double,
    redBelow: Double,
    greenAbove: Double,
    detail: String
) {
    val background = when {
        value < redBelow -> softRed
        value <= greenAbove -> softAmber
        else -> softGreen
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = background),
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(label, fontWeight = FontWeight.Bold)
            Text(
                text = percent(value),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Text(detail, style = MaterialTheme.typography.bodySmall)
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

private fun years(value: Double): String =
    NumberFormat.getNumberInstance(Locale("es", "ES")).apply {
        minimumFractionDigits = 1
        maximumFractionDigits = 1
    }.format(value) + " años"
