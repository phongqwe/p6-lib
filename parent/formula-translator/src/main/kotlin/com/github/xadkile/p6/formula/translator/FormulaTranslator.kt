package com.github.xadkile.p6.formula.translator

import com.github.michaelbull.result.Result

interface FormulaTranslator {
    fun translate(formula:String): Result<String, Exception>
}
