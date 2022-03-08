package com.github.xadkile.p6.formula.translator

import com.github.michaelbull.result.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

internal class FormulaTranslatorFinalTest {

    @Test
    fun translate_Ok() {
        val f = PythonLangElements.wsfunctionPrefix
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

        val directLiteral = mapOf(
            "123" to "123",
            "123abc" to "\"\"\"123abc\"\"\"",
            "\"abc\"" to "\"\"\"abc\"\"\"",
            "abc" to "\"\"\"abc\"\"\"",
            "\"abc" to "\"\"\"\"abc\"\"\"",
            "     abc" to "\"\"\"     abc\"\"\"",
            "abc\nccc\n\t" to "\"\"\"abc\nccc\n\t\"\"\"",
        )

        val functionLiteralInput = mapOf(
            "=sum()" to "${f}.sum()",
            """=sum(1,2,3.3)""" to """${f}.sum(1,2,3.3)""",
            """=sum(1,2,3.3,"abc")""" to """${f}.sum(1,2,3.3,"abc")""",
            """=f1(1,2,3.3,"qwe",f2())""" to """${f}.f1(1,2,3.3,"qwe",${f}.f2())""",
            """=f1(1,2,3.3,"qwe",f2(1,"ab"))""" to """${f}.f1(1,2,3.3,"qwe",${f}.f2(1,"ab"))""",
            """=mf(1+2,"b",-1)""" to """${f}.mf(1+2,"b",-1)""",
        )

        val range = mapOf(
            "=f(A1)" to """${f}.f(cell("@A1").value)""",
            "=f(AB1123)" to """${f}.f(cell("@AB1123").value)""",
            "=f(A1:C4)" to """${f}.f(getRange("@A1:C4"))""",
            "=f(AK11:CX34)" to """${f}.f(getRange("@AK11:CX34"))""",
            "=f(1:123)" to """${f}.f(getRange("@1:123"))""",
            "=f(A:b)" to """${f}.f(getRange("@A:b"))""",
            "=f(sheet1!A:b)" to """${f}.f(getSheet("sheet1").getRange("@A:b"))""",
            "=f(sheet1!A123)" to """${f}.f(getSheet("sheet1").cell("@A123").value)""",
            "=f('sheet1 23'!A123)" to """${f}.f(getSheet("sheet1 23").cell("@A123").value)""",
        )

        val composite = mapOf(
            """=f1(1,A1,B34:z9, "zzz",-234,1+2*3,C1)""" to """
                ${f}.f1(1,cell("@A1").value,getRange("@B34:z9"),"zzz",-234,1+2*3,cell("@C1").value)
            """.trimIndent(),
            """=f1(f2(),f3(f4(),f5()))""" to """${f}.f1(${f}.f2(),${f}.f3(${f}.f4(),${f}.f5()))""",
            """=f1(f2(1,2^7*9,"A1"),f3(f4(1+f9()),f5("az"+f9())))""" to """${f}.f1(${f}.f2(1,2**7*9,"A1"),${f}.f3(${f}.f4(1+${f}.f9()),${f}.f5("az"+${f}.f9())))""",
        )

        val scripts = mapOf(
            """=SCRIPT(my f1() script 123)""" to """my f1() script 123""",
            """=script(my f1() script 123)""" to """my f1() script 123""",
            """=sCriPT(my f1() script 123)""" to """my f1() script 123""",
            """
                =SCRIPT(x=1;
                while x < 10:
                    x= x+1
                x)
            """.trimIndent() to "x=1;\n"+"while x < 10:\n"+"    x= x+1\n"+"x"
        )
        val all = literalInput + functionLiteralInput + range +composite+scripts + directLiteral
        val translator = FormulaTranslatorFinal()
        for ((i, o) in directLiteral) {
            val ors = translator.translate(i)
            assertTrue(ors is Ok, ors.getError().toString())
            assertEquals(o, ors.get(),i)
        }
    }

    @Test
    fun translate_Fail(){
        val scripts = listOf(
            "a",
            "abc",
            "=f(123",
            """=f(1,"a")2+3""",
            """1+1+2""",
            "\"a\"",
            "123",
            "---",
            "@#$123",
            "=23!",
            """=f1(f2(1,2^7*9,"A1"),f3(f4(1+f9(),f5("az"+f9())))""",
            "f(sheet1!A123)",
            "f(sheet1!A123)",
            "=f(sheet1 23!A123)",
            """=sum(1,2,3.3,abc)""",
            """=sum(1,2,3.3,abc")""",
            """=sum(1,2,3.3,"abc)""",
        )
        val translator = PythonFormulaTranslator()
        for (i in scripts) {
            val ors = translator.translate(i)
            assertTrue(ors is Err,i)
        }
    }
}
