package com.antoniomancebo.rentabilidadinmueble.calculator

import org.junit.Assert.assertEquals
import org.junit.Test

class InvestmentCalculatorTest {

    @Test
    fun original_workbook_mortgage_1_matches_exact_values() {
        val inputs = InvestmentInputs()
        val result = InvestmentCalculator.mortgage(
            inputs,
            MortgageScenario("1", 30, 2.44)
        )

        assertEquals(54_000.00, result.principal, 0.01)
        assertEquals(22_206.44, result.totalInterest, 0.01)
        assertEquals(1_800.00, result.averagePrincipalPerYear, 0.01)
        assertEquals(740.21, result.averageInterestPerYear, 0.01)
        assertEquals(150.00, result.averagePrincipalPerMonth, 0.01)
        assertEquals(61.68, result.averageInterestPerMonth, 0.01)
        assertEquals(211.68, result.monthlyPayment, 0.01)
    }

    @Test
    fun original_workbook_mortgage_2_matches_exact_values() {
        val inputs = InvestmentInputs()
        val result = InvestmentCalculator.mortgage(
            inputs,
            MortgageScenario("2", 30, 1.55)
        )

        assertEquals(54_000.00, result.principal, 0.01)
        assertEquals(13_558.77, result.totalInterest, 0.01)
        assertEquals(451.96, result.averageInterestPerYear, 0.01)
        assertEquals(37.66, result.averageInterestPerMonth, 0.01)
        assertEquals(187.66, result.monthlyPayment, 0.01)
    }

    @Test
    fun original_workbook_investment_model_matches_exact_values() {
        val inputs = InvestmentInputs()
        val mortgage = InvestmentCalculator.mortgage(
            inputs,
            MortgageScenario("1", 30, 2.44)
        )

        val result = InvestmentCalculator.investment(inputs, mortgage)

        assertEquals(6_000.00, result.downPayment, 0.01)
        assertEquals(1_200.00, result.notaryRegistryValuationManagement, 0.01)
        assertEquals(4_200.00, result.transferTax, 0.01)
        assertEquals(66_400.00, result.acquisitionCost, 0.01)
        assertEquals(5_880.00, result.annualRent, 0.01)
        assertEquals(8.8554216869, result.grossYieldPercent, 0.000001)

        assertEquals(11_400.00, result.cashNeededForPurchase, 0.01)
        assertEquals(12_400.00, result.cashNeededIncludingReforms, 0.01)

        assertEquals(294.00, result.maintenanceAnnual, 0.01)
        assertEquals(294.00, result.vacancyAnnual, 0.01)
        assertEquals(740.214784, result.mortgageInterestAnnual, 0.0001)

        assertEquals(3_862.155216, result.preTaxBenefit, 0.0001)
        assertEquals(321.846268, result.preTaxBenefitMonthly, 0.0001)
        assertEquals(5.8164988192, result.netYieldAiPercent, 0.000001)

        assertEquals(732.00, result.depreciationAnnual, 0.01)
        assertEquals(1_252.062086, result.taxableBaseAfterHomeReduction, 0.0001)
        assertEquals(375.618626, result.incomeTax, 0.0001)

        assertEquals(3_486.536590, result.netBenefitDi, 0.0001)
        assertEquals(290.544716, result.netBenefitDiMonthly, 0.0001)
        assertEquals(5.2508081175, result.netYieldDiPercent, 0.000001)

        assertEquals(2_062.155216, result.cashFlowAi, 0.0001)
        assertEquals(171.846268, result.cashFlowAiMonthly, 0.0001)
        assertEquals(1_686.536590, result.cashFlowDi, 0.0001)
        assertEquals(140.544716, result.cashFlowDiMonthly, 0.0001)

        assertEquals(31.1464130320, result.rocePercent, 0.000001)
        assertEquals(3.2106425834, result.roceYears, 0.000001)
        assertEquals(16.6302839997, result.cashOnCashPercent, 0.000001)
        assertEquals(6.0131264145, result.cashOnCashYears, 0.000001)
    }

    @Test
    fun pasted_45000_example_still_matches() {
        val inputs = InvestmentInputs(
            purchasePrice = 45_000.0,
            downPaymentPercent = 10.0,
            reforms = 0.0,
            monthlyRent = 0.0
        )

        val result = InvestmentCalculator.mortgage(
            inputs,
            MortgageScenario("1", 30, 2.44)
        )

        assertEquals(40_500.00, result.principal, 0.01)
        assertEquals(16_654.83, result.totalInterest, 0.01)
        assertEquals(158.76, result.monthlyPayment, 0.01)
    }
}
