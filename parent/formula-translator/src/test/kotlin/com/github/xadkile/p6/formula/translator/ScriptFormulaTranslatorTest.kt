package com.github.xadkile.p6.formula.translator

import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.get
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

internal class ScriptFormulaTranslatorTest {

    @Test
    fun translate() {
        val scripts = mapOf(
            """=SCRIPT(my script)""" to """my script""",
            """=script(my script)""" to """my script""",
            """=sCriPT(myscript 123)""" to """myscript 123"""
        )
        val translator = ScriptFormulaTranslator()
        for ((i, o) in scripts) {
            val ors = translator.translate(i)
            assertTrue(ors is Ok,i)
            assertEquals(o, ors.get())
        }
    }
}
