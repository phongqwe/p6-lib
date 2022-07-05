package com.emeraldblast.p6.formula.translator

import com.emeraldblast.p6.common.exception.error.ErrorReport
import com.github.michaelbull.result.Result

interface P6Translator<T> {
    fun translate(formula:String): Result<T, ErrorReport>
}
