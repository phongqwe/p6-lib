package com.github.xadkile.p6.formula.translator

import com.github.michaelbull.result.Result
import com.github.xadkile.p6.exception.lib.error.ErrorReport
import java.util.regex.Pattern

class NonFormulaTranslator:FormulaTranslator {
    companion object{
        val codePattern = Pattern.compile("\"+\"", Pattern.CASE_INSENSITIVE or Pattern.DOTALL or Pattern.MULTILINE)
    }
    override fun translate(formula: String): Result<String, ErrorReport> {
        TODO("Not yet implemented")
    }
}
