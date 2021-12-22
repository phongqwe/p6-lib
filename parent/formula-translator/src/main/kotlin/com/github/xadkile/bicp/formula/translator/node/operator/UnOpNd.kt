package com.github.xadkile.bicp.formula.translator.node.operator

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import com.github.xadkile.bicp.exception.ExceptionInfo
import com.github.xadkile.bicp.formula.translator.node.operator.exception.InvalidOperatorException

/**
 * unarity operator node
 */
class UnOpNd private constructor(val operator: Char) {
    companion object {
        val validUnOp = listOf('+', '-')

        fun from(operator: Char): Result<UnOpNd, Exception> {
            if (validUnOp.contains(operator)) {
                return Ok(UnOpNd(operator))
            } else {
                val er = Err(
                    InvalidOperatorException(
                        ExceptionInfo(
                            msg = "Invalid unary operator: $operator",
                            loc = "UnOpNd",
                            data = operator
                        )
                    )
                )
                return er
            }
        }
    }
}
