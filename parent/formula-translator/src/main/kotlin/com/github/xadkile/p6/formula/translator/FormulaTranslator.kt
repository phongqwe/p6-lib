package com.github.xadkile.p6.formula.translator

import com.github.michaelbull.result.Result
import com.github.xadkile.p6.common.exception.lib.error.ErrorReport

interface FormulaTranslator {
    fun translate(formula:String): Result<String, ErrorReport>
}
