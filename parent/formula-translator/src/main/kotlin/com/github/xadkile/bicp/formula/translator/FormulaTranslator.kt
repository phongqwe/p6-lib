package com.github.xadkile.bicp.formula.translator

import com.github.michaelbull.result.Result

interface FormulaTranslator {
    fun translate(formula:String): Result<String, Exception>
}
