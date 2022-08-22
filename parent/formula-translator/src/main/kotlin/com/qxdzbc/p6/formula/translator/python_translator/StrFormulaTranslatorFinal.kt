package com.qxdzbc.p6.formula.translator.python_translator

import com.github.michaelbull.result.Result
import com.qxdzbc.p6.common.exception.error.ErrorReport

class StrFormulaTranslatorFinal : StrFormulaTranslator {
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
