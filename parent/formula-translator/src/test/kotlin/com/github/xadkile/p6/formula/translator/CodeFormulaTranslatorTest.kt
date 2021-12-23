package com.github.xadkile.p6.formula.translator

import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.get
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue


internal class CodeFormulaTranslatorTest {

    @Test
    fun translate() {
        val code = "=CODE(x=12;y=x*2)"
        val o = CodeFormulaTranslator().translate(code)
        assertTrue(o is Ok)
        assertEquals("x=12;y=x*2",o.value)
    }
}
