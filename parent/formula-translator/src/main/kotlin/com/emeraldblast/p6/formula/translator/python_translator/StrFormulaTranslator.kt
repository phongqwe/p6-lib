package com.emeraldblast.p6.formula.translator.python_translator

import com.github.michaelbull.result.Result
import com.emeraldblast.p6.common.exception.error.ErrorReport

/**
 * translate a formula to a string
 */
interface StrFormulaTranslator {
    fun translate(formula:String): Result<String, ErrorReport>
}
