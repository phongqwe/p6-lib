package com.github.xadkile.p6.formula.translator

import com.github.michaelbull.result.Result
import com.github.xadkile.p6.common.exception.error.ErrorReport

class FormulaTranslatorFinal : FormulaTranslator {
    private val pythonTranslator = PythonFormulaTranslator()
    private val directLiteralTranslator = DirectLiteralTranslator()
    override fun translate(formula: String): Result<String, ErrorReport> {
        val trimmed = formula.trim()
        val isFormula = trimmed.startsWith("=")
        if(isFormula){
            return pythonTranslator.translate(formula)
        }else{
            return directLiteralTranslator.translate(formula)
        }
    }
}
