package com.github.xadkile.bicp.formula.translator.node.operator

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

internal class BinOpNdTest{
    @Test
    fun testFrom() {
        for (o in BinOpNd.validBinOp) {
            val opNd = BinOpNd.from(o)
            assertTrue(opNd is Ok, o.toString())
        }

        val invalidOperators = (0..255).map { it.toChar() }.filter { BinOpNd.validBinOp.contains(it).not() }
        for (c in invalidOperators) {
            val opNd = BinOpNd.from(c)
            assertTrue(opNd is Err, c.toString())
        }
    }
}
