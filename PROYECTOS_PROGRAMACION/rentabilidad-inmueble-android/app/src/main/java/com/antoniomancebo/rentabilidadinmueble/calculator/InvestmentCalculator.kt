package com.antoniomancebo.rentabilidadinmueble.calculator

import kotlin.math.pow

data class InvestmentInputs(
    val purchasePrice: Double = 45_000.0,
    val downPaymentPercent: Double = 10.0,
    val reforms: Double = 0.0,
    val agencyCommission: Double = 0.0,
    val monthlyRent: Double = 0.0
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
    val acquisitionCost: Double,
    val cashInvested: Double,
    val annualRent: Double,
    val netYieldDiPercent: Double,
    val cashOnCashPercent: Double
)

object InvestmentCalculator {

    fun mortgage(inputs: InvestmentInputs, scenario: MortgageScenario): MortgageResult {
        val financingPercent = (100.0 - inputs.downPaymentPercent).coerceIn(0.0, 100.0)
        val principal = inputs.purchasePrice * financingPercent / 100.0
        val months = (scenario.years * 12).coerceAtLeast(1)
        val monthlyRate = scenario.tinPercent / 100.0 / 12.0

        val payment = when {
            principal <= 0.0 -> 0.0
            monthlyRate == 0.0 -> principal / months
            else -> principal * monthlyRate / (1.0 - (1.0 + monthlyRate).pow(-months))
        }

        val totalInterest = payment * months - principal

        // El Excel mostrado reparte capital e intereses totales de forma media por año/mes.
        // No es la distribución real de cada cuota del cuadro de amortización.
        val avgPrincipalYear = principal / scenario.years.coerceAtLeast(1)
        val avgInterestYear = totalInterest / scenario.years.coerceAtLeast(1)

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

    fun investment(
        inputs: InvestmentInputs,
        mortgage: MortgageResult
    ): InvestmentResult {
        val acquisitionCost = inputs.purchasePrice + inputs.reforms + inputs.agencyCommission
        val downPayment = inputs.purchasePrice * inputs.downPaymentPercent.coerceIn(0.0, 100.0) / 100.0
        val cashInvested = downPayment + inputs.reforms + inputs.agencyCommission
        val annualRent = inputs.monthlyRent * 12.0

        /*
         * IMPORTANTE:
         * Estas dos fórmulas son provisionales hasta importar el Excel original.
         * Se han aislado aquí precisamente para poder sustituirlas por las fórmulas
         * exactas de las celdas sin tocar la interfaz ni la lógica hipotecaria.
         */
        val netYieldDi = if (acquisitionCost > 0.0) annualRent / acquisitionCost * 100.0 else 0.0
        val annualCashFlowAfterDebt = annualRent - mortgage.annualDebtService
        val cashOnCash = if (cashInvested > 0.0) annualCashFlowAfterDebt / cashInvested * 100.0 else 0.0

        return InvestmentResult(
            acquisitionCost = acquisitionCost,
            cashInvested = cashInvested,
            annualRent = annualRent,
            netYieldDiPercent = netYieldDi,
            cashOnCashPercent = cashOnCash
        )
    }
}
