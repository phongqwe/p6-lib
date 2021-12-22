package com.github.xadkile.p6.formula.translator

import com.github.michaelbull.result.Ok
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

internal class NaiveFormulaTranslatorTest{
    @Test
    fun testSumPattern(){
        val pattern = NaiveFormulaTranslator.sumPattern
        val o1 = pattern.matcher("SUM(A1:BC333)").matches()
        val o2 = pattern.matcher("SUM(A1)").matches()
        assertTrue(o1)
        assertTrue(o2)
    }

    @Test
    fun testAddressPattern(){
        val p = NaiveFormulaTranslator.addressPattern
        val o1 = p.matcher("A1:B33").matches()
        assertTrue(o1)
        val z = "SUM(A1:BC333)"
        val o2 = p.matcher(z)
        o2.find()
        println(z.substring(o2.start(0),o2.end(0)))
    }

    @Test
    fun testTranslate(){
        val input= "SUM(A1:A10)"
        val out= NaiveFormulaTranslator().translate(input)
        assertTrue(out is Ok)
        assertEquals("WorksheetFunctions.SUM(getRange(\"@A1:A10\"))",out.value)
    }
}
