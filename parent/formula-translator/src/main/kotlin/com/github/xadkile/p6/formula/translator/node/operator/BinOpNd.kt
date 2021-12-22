package com.github.xadkile.p6.formula.translator.node.operator

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import com.github.xadkile.p6.exception.ExceptionInfo
import com.github.xadkile.p6.formula.translator.node.operator.exception.InvalidOperatorException

/**
 * Binary operator node
 */
class BinOpNd private constructor(val operator: Char) {
    companion object {
        val validBinOp = listOf('+', '-','*','/','%')

        fun from(operator: Char): Result<BinOpNd, Exception> {
            if (validBinOp.contains(operator)) {
                return Ok(BinOpNd(operator))
            } else {
                val er = Err(
                    InvalidOperatorException(
                        ExceptionInfo(
                            msg = "Invalid binary operator: $operator",
                            loc = "BinOpNd",
                            data = operator
                        )
                    )
                )
                return er
            }
        }
    }
}
