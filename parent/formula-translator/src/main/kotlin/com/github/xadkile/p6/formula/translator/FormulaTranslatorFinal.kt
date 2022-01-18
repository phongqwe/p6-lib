package com.github.xadkile.p6.formula.translator

import com.github.michaelbull.result.Result
import com.github.xadkile.p6.exception.lib.error.ErrorReport

class FormulaTranslatorFinal : FormulaTranslator {
    val pythonTranslator = PythonFormulaTranslator()
    val nonFormulaTranslator = NonFormulaTranslator()
    override fun translate(formula: String): Result<String, ErrorReport> {
        val trimmed = formula.trim()
        val isFormula = trimmed.startsWith("=")
        if(isFormula){
            return pythonTranslator.translate(formula)
        }else{
            return nonFormulaTranslator.translate(formula)
        }
    }
}
