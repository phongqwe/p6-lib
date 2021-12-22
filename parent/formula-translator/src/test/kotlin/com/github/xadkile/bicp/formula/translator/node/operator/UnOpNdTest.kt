package com.github.xadkile.bicp.formula.translator.node.operator

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

internal class UnOpNdTest {
    @Test
    fun testFrom() {
        for (o in UnOpNd.validUnOp) {
            val opNd = UnOpNd.from(o)
            assertTrue(opNd is Ok, o.toString())
        }

        val invalidOperators = (0..255).map { it.toChar() }.filter { UnOpNd.validUnOp.contains(it).not() }
        for (c in invalidOperators) {
            val opNd = UnOpNd.from(c)
            assertTrue(opNd is Err, c.toString())
        }
    }
}
