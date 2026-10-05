package com.antoniomancebo.rentabilidadinmueble.calculator

import org.junit.Assert.assertEquals
import org.junit.Test

class InvestmentCalculatorTest {

    private val inputs = InvestmentInputs(
        purchasePrice = 45_000.0,
        downPaymentPercent = 10.0
    )

    @Test
    fun mortgage_244_matches_excel_example() {
        val result = InvestmentCalculator.mortgage(
            inputs,
            MortgageScenario("1", 30, 2.44)
        )

        assertEquals(40_500.00, result.principal, 0.01)
        assertEquals(16_654.83, result.totalInterest, 0.01)
        assertEquals(1_350.00, result.averagePrincipalPerYear, 0.01)
        assertEquals(555.16, result.averageInterestPerYear, 0.01)
        assertEquals(112.50, result.averagePrincipalPerMonth, 0.01)
        assertEquals(46.26, result.averageInterestPerMonth, 0.01)
        assertEquals(158.76, result.monthlyPayment, 0.01)
    }

    @Test
    fun mortgage_155_matches_excel_example() {
        val result = InvestmentCalculator.mortgage(
            inputs,
            MortgageScenario("2", 30, 1.55)
        )

        assertEquals(40_500.00, result.principal, 0.01)
        assertEquals(10_169.08, result.totalInterest, 0.01)
        assertEquals(338.97, result.averageInterestPerYear, 0.01)
        assertEquals(28.25, result.averageInterestPerMonth, 0.01)
        assertEquals(140.75, result.monthlyPayment, 0.01)
    }
}
