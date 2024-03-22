package com.codesignal

import com.share.codesignal.CoverDebts
import io.kotest.core.spec.style.FunSpec
import io.kotest.core.spec.style.Test
import io.kotest.matchers.shouldBe


@Test
class CoverDebtsTest : FunSpec({
    context("coverDebts") {
        test("Test case 1") {
            val salary = 50.0
            val debts = doubleArrayOf(2.0, 2.0, 5.0)
            val interest = doubleArrayOf(200.0, 100.0, 150.0)

            val result = CoverDebts.minDebtPayment(salary, debts, interest)

            result shouldBe 22.0
        }
    }
})
