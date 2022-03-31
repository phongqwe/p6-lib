package com.emeraldblast.p6.formula.translator

import com.github.michaelbull.result.Result
import com.emeraldblast.p6.common.exception.error.ErrorReport

interface FormulaTranslator {
    fun translate(formula:String): Result<String, ErrorReport>
}
