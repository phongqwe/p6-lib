package com.github.xadkile.p6.formula.translator

import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.get
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*

internal class FormulaTranslatorImpTest {

    @Test
    fun translate() {
        val f = LanguageConst.wsfunction

        val literalInput = mapOf(
            "=123" to "123",
            "=123.123" to "123.123",
            "=-123" to "-123",
            "=+123" to "+123",
            "=0-123" to "0-123",
            "=2^3^4" to "2**3**4",
            "=1+2" to "1+2",
            "=1+2.3+4" to "1+2.3+4",
            "=(-12+33)*9-(((222)))" to "(-12+33)*9-(((222)))",
            "=\"\"" to "\"\"",
            "=\"abc\"" to "\"abc\"",
            "=\"123\"" to "\"123\"",
            "=\"abc\"+1" to "\"abc\"+1",
            "=\"abc\"+1+\"x\"" to "\"abc\"+1+\"x\"",
            "=123+\"qwe\"" to "123+\"qwe\"",
            "=2^3^4" to "2**3**4",
            "=2^(2+3-1)*9" to "2**(2+3-1)*9",
            "=2^(-1)" to "2**(-1)",
        )

        val functionLiteralInput = mapOf(
            "=sum()" to "${f}.sum()",
            "=sum(1,2,3.3)" to "${f}.sum(1,2,3.3)",
            "=sum(1,2,3.3,\"abc\")" to "${f}.sum(1,2,3.3,\"abc\")",
            "=f1(1,2,3.3,\"qwe\",f2())" to "${f}.f1(1,2,3.3,\"qwe\",${f}.f2())",
            "=f1(1,2,3.3,\"qwe\",f2(1,\"ab\"))" to "${f}.f1(1,2,3.3,\"qwe\",${f}.f2(1,\"ab\"))",
            "=mf(1+2,\"b\",-1)" to "${f}.mf(1+2,\"b\",-1)",
        )

        val translator = FormulaTranslatorImp()
        for ((i, o) in literalInput + functionLiteralInput) {
            val ors = translator.translate(i)
            assertTrue(ors is Ok)
            assertEquals(o, ors.get())
        }
    }
}
