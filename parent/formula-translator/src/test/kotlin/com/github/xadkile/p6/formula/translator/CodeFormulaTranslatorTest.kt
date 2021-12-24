package com.github.xadkile.p6.formula.translator

import com.github.michaelbull.result.Ok
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue


internal class CodeFormulaTranslatorTest {

    @Test
    fun translate() {
        val code = "=SCRIPT(x=12;y=x*2)"
        val o = ScriptFormulaTranslator().translate(code)
        assertTrue(o is Ok)
        assertEquals("x=12;y=x*2",o.value)
    }
}
