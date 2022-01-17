package com.github.xadkile.p6.formula.template

interface FormulaTemplate {
    fun executionBlock(code: String, workbook: String): String
}

