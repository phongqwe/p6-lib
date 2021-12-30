package com.github.xadkile.p6.formula.translator

import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import com.github.xadkile.p6.exception.error.ErrorReport
import com.github.xadkile.p6.formula.translator.antlr.FormulaLexer
import com.github.xadkile.p6.formula.translator.antlr.FormulaParser
import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream
import org.antlr.v4.runtime.tree.ParseTree

class FormulaTranslatorImp : FormulaTranslator {
    override fun translate(formula: String): Result<String, ErrorReport> {
        val charStream = CharStreams.fromString(formula)
        val lexer = FormulaLexer(charStream)
        val zzz = CommonTokenStream(lexer)
        val parser = FormulaParser(zzz)
        val tree: ParseTree = parser.formula()
        val visitor = PythonFormularVisitor()
        val out=visitor.visit(tree)
        return Ok(out)
    }
}
