package com.github.xadkile.p6.formula.template

import com.github.xadkile.p6.formula.translator.FormulaMapper
import com.github.xadkile.p6.formula.translator.PythonMapper

object PythonTemplate : FormulaTemplate {
    private val mapper: FormulaMapper = PythonMapper
    override fun executionBlock(code: String, workbook: String): String {
        return code + "\n" +
                mapper.getWorkbook(workbook) + ".reRun()" + "\n" +
                "str(${mapper.getWorkbook(workbook) }.toJson())"
    }
}
