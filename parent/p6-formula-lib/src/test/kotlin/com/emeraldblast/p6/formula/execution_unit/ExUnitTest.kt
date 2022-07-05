package com.emeraldblast.p6.formula.execution_unit

import com.emeraldblast.p6.formula.FunctionMap
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

internal class ExUnitTest {

    class IntNum {
        @Test
        fun run() {
            val u = ExUnit.IntNum(100)
            assertEquals(100, u.run())
        }
    }

    internal class Func {
        fun add(n1: Int, n2: Int): Int {
            return n1 + n2
        }

        fun toUpper(str: String): String {
            return str.uppercase()
        }
        val fMap = mock<FunctionMap>{
            whenever(it.getFunc("toUpper")).thenReturn(::toUpper)
            whenever(it.getFunc("add")).thenReturn(::add)
        }
        @Test
        fun run() {
            val u1 = ExUnit.Func(
                funcName = "toUpper",
                args = listOf(
                    ExUnit.Text("abc")
                ),
                functionMap = fMap
            )
            val out = u1.run()
            assertEquals("ABC",out)

            val u2 = ExUnit.Func(
                funcName = "add",
                args = listOf(
                    ExUnit.IntNum(3),
                    ExUnit.IntNum(4),
                ),
                functionMap = fMap
            )
            assertEquals(7,u2.run())
        }

        @Test
        fun run2() {
            val u2 = ExUnit.Func(
                funcName = "add",
                args = listOf(
                    ExUnit.Func(
                        funcName = "add",
                        args = listOf(
                            ExUnit.IntNum(3),
                            ExUnit.Func(
                                funcName = "add",
                                args = listOf(
                                    ExUnit.IntNum(2),
                                    ExUnit.IntNum(2),
                                ),
                                functionMap = fMap
                            ),
                        ),
                        functionMap = fMap
                    ),
                    ExUnit.IntNum(5),
                ),
                functionMap = fMap
            )
            assertEquals(3+2+2+5, u2.run())
        }
    }
}
