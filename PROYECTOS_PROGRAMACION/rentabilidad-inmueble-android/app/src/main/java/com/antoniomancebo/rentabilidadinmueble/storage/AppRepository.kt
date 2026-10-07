package com.antoniomancebo.rentabilidadinmueble.storage

import android.content.Context
import com.antoniomancebo.rentabilidadinmueble.calculator.InvestmentInputs
import com.antoniomancebo.rentabilidadinmueble.calculator.InvestmentResult
import com.antoniomancebo.rentabilidadinmueble.calculator.MortgageResult
import com.antoniomancebo.rentabilidadinmueble.calculator.MortgageScenario
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class SavedPropertyReport(
    val id: Long,
    val createdAt: Long,
    val name: String,
    val inputs: InvestmentInputs,
    val scenarios: List<MortgageScenario>,
    val mortgages: List<MortgageResult>,
    val result: InvestmentResult
)

data class ShoppingItem(
    val id: Long,
    val createdAt: Long,
    val title: String,
    val category: String,
    val url: String,
    val itemPrice: Double,
    val shippingPrice: Double,
    val notes: String = ""
) {
    val totalPrice: Double get() = itemPrice + shippingPrice
}

class AppRepository(context: Context) {
    private val prefs = context.getSharedPreferences("rentabilidad_inmueble_data", Context.MODE_PRIVATE)

    fun loadReports(): List<SavedPropertyReport> =
        parseArray(prefs.getString(KEY_REPORTS, "[]")).mapNotNull(::reportFromJson)
            .sortedByDescending { it.createdAt }

    fun saveReport(
        name: String,
        inputs: InvestmentInputs,
        scenarios: List<MortgageScenario>,
        mortgages: List<MortgageResult>,
        result: InvestmentResult
    ): SavedPropertyReport {
        val now = System.currentTimeMillis()
        val fallback = "Informe " + SimpleDateFormat("dd/MM/yyyy HH:mm", Locale("es", "ES")).format(Date(now))
        val report = SavedPropertyReport(
            id = now,
            createdAt = now,
            name = name.trim().ifBlank { fallback },
            inputs = inputs,
            scenarios = scenarios,
            mortgages = mortgages,
            result = result
        )
        val current = loadReports().toMutableList()
        current.add(0, report)
        writeArray(KEY_REPORTS, current.map(::reportToJson))
        return report
    }

    fun deleteReport(id: Long) {
        writeArray(KEY_REPORTS, loadReports().filterNot { it.id == id }.map(::reportToJson))
    }

    fun loadShoppingItems(): List<ShoppingItem> =
        parseArray(prefs.getString(KEY_SHOPPING, "[]")).mapNotNull(::shoppingFromJson)

    fun saveShoppingItem(
        title: String,
        category: String,
        url: String,
        itemPrice: Double,
        shippingPrice: Double,
        notes: String
    ): ShoppingItem {
        val now = System.currentTimeMillis()
        val item = ShoppingItem(
            id = now,
            createdAt = now,
            title = title.trim().ifBlank { "Artículo" },
            category = category.trim().ifBlank { "General" },
            url = url.trim(),
            itemPrice = itemPrice.coerceAtLeast(0.0),
            shippingPrice = shippingPrice.coerceAtLeast(0.0),
            notes = notes.trim()
        )
        val current = loadShoppingItems().toMutableList()
        current.add(item)
        writeArray(KEY_SHOPPING, current.map(::shoppingToJson))
        return item
    }

    fun deleteShoppingItem(id: Long) {
        writeArray(KEY_SHOPPING, loadShoppingItems().filterNot { it.id == id }.map(::shoppingToJson))
    }

    private fun reportToJson(report: SavedPropertyReport): JSONObject = JSONObject().apply {
        put("id", report.id)
        put("createdAt", report.createdAt)
        put("name", report.name)
        put("inputs", inputsToJson(report.inputs))
        put("scenarios", JSONArray().apply { report.scenarios.forEach { put(scenarioToJson(it)) } })
        put("mortgages", JSONArray().apply { report.mortgages.forEach { put(mortgageToJson(it)) } })
        put("result", resultToJson(report.result))
    }

    private fun reportFromJson(json: JSONObject): SavedPropertyReport? = runCatching {
        SavedPropertyReport(
            id = json.getLong("id"),
            createdAt = json.getLong("createdAt"),
            name = json.getString("name"),
            inputs = inputsFromJson(json.getJSONObject("inputs")),
            scenarios = json.getJSONArray("scenarios").toObjects(::scenarioFromJson),
            mortgages = json.getJSONArray("mortgages").toObjects(::mortgageFromJson),
            result = resultFromJson(json.getJSONObject("result"))
        )
    }.getOrNull()

    private fun shoppingToJson(item: ShoppingItem): JSONObject = JSONObject().apply {
        put("id", item.id)
        put("createdAt", item.createdAt)
        put("title", item.title)
        put("category", item.category)
        put("url", item.url)
        put("itemPrice", item.itemPrice)
        put("shippingPrice", item.shippingPrice)
        put("notes", item.notes)
    }

    private fun shoppingFromJson(json: JSONObject): ShoppingItem? = runCatching {
        ShoppingItem(
            id = json.getLong("id"),
            createdAt = json.getLong("createdAt"),
            title = json.getString("title"),
            category = json.getString("category"),
            url = json.getString("url"),
            itemPrice = json.getDouble("itemPrice"),
            shippingPrice = json.getDouble("shippingPrice"),
            notes = json.optString("notes")
        )
    }.getOrNull()

    private fun inputsToJson(v: InvestmentInputs) = JSONObject().apply {
        put("purchasePrice", v.purchasePrice)
        put("downPaymentPercent", v.downPaymentPercent)
        put("reforms", v.reforms)
        put("agencyCommission", v.agencyCommission)
        put("monthlyRent", v.monthlyRent)
        put("rentDefaultInsuranceAnnual", v.rentDefaultInsuranceAnnual)
        put("wasteTaxAnnual", v.wasteTaxAnnual)
        put("homeInsuranceAnnual", v.homeInsuranceAnnual)
        put("lifeInsuranceAnnual", v.lifeInsuranceAnnual)
        put("communityAnnual", v.communityAnnual)
        put("ibiAnnual", v.ibiAnnual)
        put("incomeTaxPercent", v.incomeTaxPercent)
        put("constructionValuePercent", v.constructionValuePercent)
    }

    private fun inputsFromJson(j: JSONObject) = InvestmentInputs(
        purchasePrice = j.getDouble("purchasePrice"),
        downPaymentPercent = j.getDouble("downPaymentPercent"),
        reforms = j.getDouble("reforms"),
        agencyCommission = j.getDouble("agencyCommission"),
        monthlyRent = j.getDouble("monthlyRent"),
        rentDefaultInsuranceAnnual = j.getDouble("rentDefaultInsuranceAnnual"),
        wasteTaxAnnual = j.getDouble("wasteTaxAnnual"),
        homeInsuranceAnnual = j.getDouble("homeInsuranceAnnual"),
        lifeInsuranceAnnual = j.getDouble("lifeInsuranceAnnual"),
        communityAnnual = j.getDouble("communityAnnual"),
        ibiAnnual = j.getDouble("ibiAnnual"),
        incomeTaxPercent = j.getDouble("incomeTaxPercent"),
        constructionValuePercent = j.getDouble("constructionValuePercent")
    )

    private fun scenarioToJson(v: MortgageScenario) = JSONObject().apply {
        put("title", v.title)
        put("years", v.years)
        put("tinPercent", v.tinPercent)
    }

    private fun scenarioFromJson(j: JSONObject) = MortgageScenario(
        title = j.getString("title"),
        years = j.getInt("years"),
        tinPercent = j.getDouble("tinPercent")
    )

    private fun mortgageToJson(v: MortgageResult) = JSONObject().apply {
        put("financingPercent", v.financingPercent)
        put("principal", v.principal)
        put("totalInterest", v.totalInterest)
        put("averagePrincipalPerYear", v.averagePrincipalPerYear)
        put("averageInterestPerYear", v.averageInterestPerYear)
        put("averagePrincipalPerMonth", v.averagePrincipalPerMonth)
        put("averageInterestPerMonth", v.averageInterestPerMonth)
        put("monthlyPayment", v.monthlyPayment)
        put("annualDebtService", v.annualDebtService)
    }

    private fun mortgageFromJson(j: JSONObject) = MortgageResult(
        financingPercent = j.getDouble("financingPercent"),
        principal = j.getDouble("principal"),
        totalInterest = j.getDouble("totalInterest"),
        averagePrincipalPerYear = j.getDouble("averagePrincipalPerYear"),
        averageInterestPerYear = j.getDouble("averageInterestPerYear"),
        averagePrincipalPerMonth = j.getDouble("averagePrincipalPerMonth"),
        averageInterestPerMonth = j.getDouble("averageInterestPerMonth"),
        monthlyPayment = j.getDouble("monthlyPayment"),
        annualDebtService = j.getDouble("annualDebtService")
    )

    private fun resultToJson(v: InvestmentResult) = JSONObject().apply {
        put("downPayment", v.downPayment)
        put("notaryRegistryValuationManagement", v.notaryRegistryValuationManagement)
        put("transferTax", v.transferTax)
        put("acquisitionCost", v.acquisitionCost)
        put("annualRent", v.annualRent)
        put("grossYieldPercent", v.grossYieldPercent)
        put("cashNeededForPurchase", v.cashNeededForPurchase)
        put("cashNeededIncludingReforms", v.cashNeededIncludingReforms)
        put("maintenanceAnnual", v.maintenanceAnnual)
        put("vacancyAnnual", v.vacancyAnnual)
        put("mortgageInterestAnnual", v.mortgageInterestAnnual)
        put("totalAnnualExpenses", v.totalAnnualExpenses)
        put("preTaxBenefit", v.preTaxBenefit)
        put("preTaxBenefitMonthly", v.preTaxBenefitMonthly)
        put("netYieldAiPercent", v.netYieldAiPercent)
        put("depreciationAnnual", v.depreciationAnnual)
        put("taxableBaseAfterHomeReduction", v.taxableBaseAfterHomeReduction)
        put("incomeTax", v.incomeTax)
        put("netBenefitDi", v.netBenefitDi)
        put("netBenefitDiMonthly", v.netBenefitDiMonthly)
        put("netYieldDiPercent", v.netYieldDiPercent)
        put("cashFlowAi", v.cashFlowAi)
        put("cashFlowAiMonthly", v.cashFlowAiMonthly)
        put("cashFlowDi", v.cashFlowDi)
        put("cashFlowDiMonthly", v.cashFlowDiMonthly)
        put("rocePercent", v.rocePercent)
        put("roceYears", v.roceYears)
        put("cashOnCashPercent", v.cashOnCashPercent)
        put("cashOnCashYears", v.cashOnCashYears)
    }

    private fun resultFromJson(j: JSONObject) = InvestmentResult(
        downPayment = j.getDouble("downPayment"),
        notaryRegistryValuationManagement = j.getDouble("notaryRegistryValuationManagement"),
        transferTax = j.getDouble("transferTax"),
        acquisitionCost = j.getDouble("acquisitionCost"),
        annualRent = j.getDouble("annualRent"),
        grossYieldPercent = j.getDouble("grossYieldPercent"),
        cashNeededForPurchase = j.getDouble("cashNeededForPurchase"),
        cashNeededIncludingReforms = j.getDouble("cashNeededIncludingReforms"),
        maintenanceAnnual = j.getDouble("maintenanceAnnual"),
        vacancyAnnual = j.getDouble("vacancyAnnual"),
        mortgageInterestAnnual = j.getDouble("mortgageInterestAnnual"),
        totalAnnualExpenses = j.getDouble("totalAnnualExpenses"),
        preTaxBenefit = j.getDouble("preTaxBenefit"),
        preTaxBenefitMonthly = j.getDouble("preTaxBenefitMonthly"),
        netYieldAiPercent = j.getDouble("netYieldAiPercent"),
        depreciationAnnual = j.getDouble("depreciationAnnual"),
        taxableBaseAfterHomeReduction = j.getDouble("taxableBaseAfterHomeReduction"),
        incomeTax = j.getDouble("incomeTax"),
        netBenefitDi = j.getDouble("netBenefitDi"),
        netBenefitDiMonthly = j.getDouble("netBenefitDiMonthly"),
        netYieldDiPercent = j.getDouble("netYieldDiPercent"),
        cashFlowAi = j.getDouble("cashFlowAi"),
        cashFlowAiMonthly = j.getDouble("cashFlowAiMonthly"),
        cashFlowDi = j.getDouble("cashFlowDi"),
        cashFlowDiMonthly = j.getDouble("cashFlowDiMonthly"),
        rocePercent = j.getDouble("rocePercent"),
        roceYears = j.getDouble("roceYears"),
        cashOnCashPercent = j.getDouble("cashOnCashPercent"),
        cashOnCashYears = j.getDouble("cashOnCashYears")
    )

    private fun parseArray(raw: String?): List<JSONObject> = runCatching {
        val array = JSONArray(raw ?: "[]")
        List(array.length()) { array.getJSONObject(it) }
    }.getOrDefault(emptyList())

    private fun writeArray(key: String, objects: List<JSONObject>) {
        prefs.edit().putString(key, JSONArray().apply { objects.forEach { put(it) } }.toString()).apply()
    }

    private fun <T> JSONArray.toObjects(mapper: (JSONObject) -> T): List<T> =
        List(length()) { mapper(getJSONObject(it)) }

    private companion object {
        const val KEY_REPORTS = "reports"
        const val KEY_SHOPPING = "shopping_items"
    }
}
