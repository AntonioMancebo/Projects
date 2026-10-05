package com.antoniomancebo.rentabilidadinmueble.calculator

import kotlin.math.pow

data class InvestmentInputs(
    val purchasePrice: Double = 60_000.0,
    val downPaymentPercent: Double = 10.0,
    val reforms: Double = 1_000.0,
    val agencyCommission: Double = 0.0,
    val monthlyRent: Double = 490.0,
    val rentDefaultInsuranceAnnual: Double = 0.0,
    val wasteTaxAnnual: Double = 64.0,
    val homeInsuranceAnnual: Double = 200.0,
    val lifeInsuranceAnnual: Double = 0.0,
    val communityAnnual: Double = 250.0,
    val ibiAnnual: Double = 175.63,
    val incomeTaxPercent: Double = 30.0,
    val constructionValuePercent: Double = 30.0
)

data class MortgageScenario(
    val title: String,
    val years: Int,
    val tinPercent: Double
)

data class MortgageResult(
    val financingPercent: Double,
    val principal: Double,
    val totalInterest: Double,
    val averagePrincipalPerYear: Double,
    val averageInterestPerYear: Double,
    val averagePrincipalPerMonth: Double,
    val averageInterestPerMonth: Double,
    val monthlyPayment: Double,
    val annualDebtService: Double
)

data class InvestmentResult(
    val downPayment: Double,
    val notaryRegistryValuationManagement: Double,
    val transferTax: Double,
    val acquisitionCost: Double,
    val annualRent: Double,
    val grossYieldPercent: Double,
    val cashNeededForPurchase: Double,
    val cashNeededIncludingReforms: Double,
    val maintenanceAnnual: Double,
    val vacancyAnnual: Double,
    val mortgageInterestAnnual: Double,
    val totalAnnualExpenses: Double,
    val preTaxBenefit: Double,
    val preTaxBenefitMonthly: Double,
    val netYieldAiPercent: Double,
    val depreciationAnnual: Double,
    val taxableBaseAfterHomeReduction: Double,
    val incomeTax: Double,
    val netBenefitDi: Double,
    val netBenefitDiMonthly: Double,
    val netYieldDiPercent: Double,
    val cashFlowAi: Double,
    val cashFlowAiMonthly: Double,
    val cashFlowDi: Double,
    val cashFlowDiMonthly: Double,
    val rocePercent: Double,
    val roceYears: Double,
    val cashOnCashPercent: Double,
    val cashOnCashYears: Double
)

object InvestmentCalculator {
    const val NOTARY_PERCENT = 2.0
    const val TRANSFER_TAX_PERCENT = 7.0
    const val MAINTENANCE_PERCENT = 5.0
    const val VACANCY_PERCENT = 5.0
    const val HOME_RENTAL_DEDUCTION_PERCENT = 60.0
    const val DEPRECIATION_PERCENT = 3.0

    fun mortgage(inputs: InvestmentInputs, scenario: MortgageScenario): MortgageResult {
        val financingPercent = (100.0 - inputs.downPaymentPercent).coerceIn(0.0, 100.0)
        val principal = inputs.purchasePrice * financingPercent / 100.0
        val years = scenario.years.coerceAtLeast(1)
        val months = years * 12
        val monthlyRate = scenario.tinPercent / 100.0 / 12.0

        val payment = when {
            principal <= 0.0 -> 0.0
            monthlyRate == 0.0 -> principal / months
            else -> principal * monthlyRate / (1.0 - (1.0 + monthlyRate).pow(-months))
        }

        // Equivale a -CUMIPMT(TIN/12, años*12, principal, 1, años*12, 0)
        // para el total de intereses de un préstamo de cuota constante.
        val totalInterest = payment * months - principal
        val avgPrincipalYear = principal / years
        val avgInterestYear = totalInterest / years

        return MortgageResult(
            financingPercent = financingPercent,
            principal = principal,
            totalInterest = totalInterest,
            averagePrincipalPerYear = avgPrincipalYear,
            averageInterestPerYear = avgInterestYear,
            averagePrincipalPerMonth = avgPrincipalYear / 12.0,
            averageInterestPerMonth = avgInterestYear / 12.0,
            monthlyPayment = payment,
            annualDebtService = payment * 12.0
        )
    }

    /**
     * Reproduce las fórmulas de Hoja1 del Excel original.
     * La Hipoteca 1 es la que alimenta intereses, principal, cashflow y retornos.
     */
    fun investment(
        inputs: InvestmentInputs,
        primaryMortgage: MortgageResult
    ): InvestmentResult {
        val downPayment = inputs.purchasePrice * inputs.downPaymentPercent.coerceIn(0.0, 100.0) / 100.0
        val notaryCosts = inputs.purchasePrice * NOTARY_PERCENT / 100.0
        val transferTax = inputs.purchasePrice * TRANSFER_TAX_PERCENT / 100.0

        // Excel C13 = SUM(C7:C11)
        val acquisitionCost =
            inputs.purchasePrice +
                inputs.reforms +
                inputs.agencyCommission +
                notaryCosts +
                transferTax

        val annualRent = inputs.monthlyRent * 12.0
        val grossYield = percentRatio(annualRent, acquisitionCost)

        // Excel G17 = C11 + C10 + C6 + C9
        val cashNeededForPurchase =
            transferTax +
                notaryCosts +
                downPayment +
                inputs.agencyCommission

        // Excel G19 = G17 + C8
        val cashNeededIncludingReforms = cashNeededForPurchase + inputs.reforms

        val maintenance = annualRent * MAINTENANCE_PERCENT / 100.0
        val vacancy = annualRent * VACANCY_PERCENT / 100.0
        val mortgageInterest = primaryMortgage.averageInterestPerYear

        val totalAnnualExpenses =
            inputs.rentDefaultInsuranceAnnual +
                inputs.wasteTaxAnnual +
                inputs.homeInsuranceAnnual +
                inputs.lifeInsuranceAnnual +
                inputs.communityAnnual +
                inputs.ibiAnnual +
                maintenance +
                vacancy +
                mortgageInterest

        // Excel C31 = SUM(C20:C29)
        val preTaxBenefit = annualRent - totalAnnualExpenses
        val netYieldAi = percentRatio(preTaxBenefit, acquisitionCost)

        // Excel C37 = 3% * (A37*C7 + SUM(C8:C11))
        val depreciation =
            DEPRECIATION_PERCENT / 100.0 *
                (
                    inputs.constructionValuePercent / 100.0 * inputs.purchasePrice +
                        inputs.reforms +
                        inputs.agencyCommission +
                        notaryCosts +
                        transferTax
                    )

        // Excel C35 = (C31-C37)*0.4: tras una deducción del 60%, tributa el 40%.
        val taxableBaseAfterHomeReduction =
            (preTaxBenefit - depreciation) *
                (1.0 - HOME_RENTAL_DEDUCTION_PERCENT / 100.0)

        // Excel C36 es negativo; aquí se guarda como gasto fiscal positivo.
        val incomeTax =
            taxableBaseAfterHomeReduction * inputs.incomeTaxPercent.coerceAtLeast(0.0) / 100.0

        // Excel C39 = C31 + C36
        val netBenefitDi = preTaxBenefit - incomeTax
        val netYieldDi = percentRatio(netBenefitDi, acquisitionCost)

        // Excel C42/C43 restan la amortización media anual de capital (F10).
        val cashFlowAi = preTaxBenefit - primaryMortgage.averagePrincipalPerYear
        val cashFlowDi = netBenefitDi - primaryMortgage.averagePrincipalPerYear

        // Excel C44/C46 usan el cash total aportado: entrada + reforma + agencia + notaría + ITP.
        val roce = percentRatio(preTaxBenefit, cashNeededIncludingReforms)
        val cashOnCash = percentRatio(cashFlowAi, cashNeededIncludingReforms)

        return InvestmentResult(
            downPayment = downPayment,
            notaryRegistryValuationManagement = notaryCosts,
            transferTax = transferTax,
            acquisitionCost = acquisitionCost,
            annualRent = annualRent,
            grossYieldPercent = grossYield,
            cashNeededForPurchase = cashNeededForPurchase,
            cashNeededIncludingReforms = cashNeededIncludingReforms,
            maintenanceAnnual = maintenance,
            vacancyAnnual = vacancy,
            mortgageInterestAnnual = mortgageInterest,
            totalAnnualExpenses = totalAnnualExpenses,
            preTaxBenefit = preTaxBenefit,
            preTaxBenefitMonthly = preTaxBenefit / 12.0,
            netYieldAiPercent = netYieldAi,
            depreciationAnnual = depreciation,
            taxableBaseAfterHomeReduction = taxableBaseAfterHomeReduction,
            incomeTax = incomeTax,
            netBenefitDi = netBenefitDi,
            netBenefitDiMonthly = netBenefitDi / 12.0,
            netYieldDiPercent = netYieldDi,
            cashFlowAi = cashFlowAi,
            cashFlowAiMonthly = cashFlowAi / 12.0,
            cashFlowDi = cashFlowDi,
            cashFlowDiMonthly = cashFlowDi / 12.0,
            rocePercent = roce,
            roceYears = reciprocalYears(preTaxBenefit, cashNeededIncludingReforms),
            cashOnCashPercent = cashOnCash,
            cashOnCashYears = reciprocalYears(cashFlowAi, cashNeededIncludingReforms)
        )
    }

    private fun percentRatio(numerator: Double, denominator: Double): Double =
        if (denominator == 0.0) 0.0 else numerator / denominator * 100.0

    private fun reciprocalYears(annualReturn: Double, investedCash: Double): Double =
        if (annualReturn == 0.0) 0.0 else investedCash / annualReturn
}
