package com.emeraldblast.p6.formula.translator.jvm_translator

import com.emeraldblast.p6.common.exception.error.ErrorReport
import com.emeraldblast.p6.formula.execution_unit.ExUnit
import com.emeraldblast.p6.formula.translator.P6Translator
import com.emeraldblast.p6.formula.translator.antlr.FormulaBaseVisitor
import com.emeraldblast.p6.formula.translator.jvm_translator.tree_extractor.TreeExtractor
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.map

class JvmFormulaTranslator(
    private val treeExtractor: TreeExtractor,
    private val visitor: FormulaBaseVisitor<ExUnit>
) : P6Translator<ExUnit> {
    override fun translate(formula: String): Result<ExUnit, ErrorReport> {
        val t = treeExtractor.extractTree(formula)
        val r = t.map {visitor.visit(it)}
        return r
    }
}
