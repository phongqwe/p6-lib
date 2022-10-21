package com.qxdzbc.p6.formula.translator.python_translator

object FormulaTranslators {
    val standard: StrFormulaTranslator = StrFormulaTranslatorFinal()
    val directLiteralTranslator: StrFormulaTranslator =
        DirectLiteralTranslator()
}
