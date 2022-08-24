package com.qxdzbc.p6.formula.translator

import com.qxdzbc.common.error.ErrorReport
import com.github.michaelbull.result.Result

interface P6Translator<T> {
    fun translate(formula:String): Result<T, ErrorReport>
}
