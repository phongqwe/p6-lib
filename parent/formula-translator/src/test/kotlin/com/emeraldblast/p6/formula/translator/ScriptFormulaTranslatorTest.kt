package com.emeraldblast.p6.formula.translator

import com.emeraldblast.p6.formula.translator.ScriptFormulaTranslator
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.get
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

internal class ScriptFormulaTranslatorTest {

    @Test
    fun translate() {
        val scripts = mapOf(
            """    =     SCRIPT(my script)   """ to """my script""",
            """=  script(my script)       """ to """my script""",
            """    =sCriPT(myscript 123)""" to """myscript 123""",
            """
                
                      
                                                       =SCRIPT(x=1;
                f1() + f2();
                while x<10:
                    x= x+1
                x
                )       
            """.trimIndent() to "x=1;\n"+"f1() + f2();\n"+"while x<10:\n"+"    x= x+1\n"+"x"
        )
        val translator = ScriptFormulaTranslator()
        for ((i, o) in scripts) {
            val ors = translator.translate(i)
            assertTrue(ors is Ok,i)
            val output = ors.get()
            assertEquals(o, output)
        }
    }
}
