package com.emeraldblast.p6.formula.translator.jvm_translator

import com.emeraldblast.p6.formula.FunctionMap
import com.emeraldblast.p6.formula.FunctionMapImp
import com.emeraldblast.p6.formula.translator.jvm_translator.tree_extractor.TreeExtractorImp
import com.github.michaelbull.result.Ok
import kotlin.math.pow

import kotlin.test.*
internal class JvmFormulaTranslatorTest {

    lateinit var functionMap:FunctionMap
    lateinit var translator:JvmFormulaTranslator
    @BeforeTest
    fun b(){
        fun add(n1:Int,n2:Int):Int{
            return n1+n2
        }
        fun toUpper(str:String):String{
            return str.uppercase()
        }
        functionMap = FunctionMapImp(
            mapOf(
                "add" to ::add,
                "toUpper" to ::toUpper
            )
        )
        translator=JvmFormulaTranslator(
            treeExtractor = TreeExtractorImp(),
            visitor = JvmFormulaVisitor(functionMap)
        )
    }

    @Test
    fun `translate number literal`() {
        val inputMap = mapOf(
            "=123" to 123,
            "=((123))" to 123,
            "=123.32" to 123.32,
            "=(123.32)" to 123.32,
            "=-123" to -123,
            "=-(123)" to -123,
            "=(-123)" to -123,
            "=(-123.32)" to -123.32,
            "=-(123.32)" to -123.32,
            "=-(123.32)" to -123.32,
            "=-(123.32)" to -123.32,
            "=-(-123.32)" to 123.32,
        )
        testTranslate(inputMap)
    }

    @Test
    fun `translate string literal`() {
        val inputMap = mapOf(
            "=\"abc\"" to "abc",
            "=\"\"" to "",
            "=(\"qwe\")" to "qwe"
        )
        testTranslate(inputMap)
    }

    @Test
    fun `translate bool literal`() {
        val inputMap = mapOf(
            "=TRUE" to true,
            "=FALSE" to false,
            "=(TRUE)" to true,
            "=((TRUE))" to true,
        )
        testTranslate(inputMap)
    }

    @Test
    fun `translate power by`(){
        testTranslate(mapOf(
            "=2^3" to 8.0,
            "=(2^3)" to 8.0,
            "=(2.5^3)" to 2.5.pow(3),
        ))
    }


    fun testTranslate(input:Map<String,Any>){
        for ((i,o) in input){
            val output=translator.translate(i)
            assertTrue { output is Ok }
            assertEquals(o, output.component1()!!.run())
        }
    }
}
