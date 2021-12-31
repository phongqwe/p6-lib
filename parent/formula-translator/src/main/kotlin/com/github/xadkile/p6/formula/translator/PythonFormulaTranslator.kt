package com.github.xadkile.p6.formula.translator

import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import com.github.xadkile.p6.exception.error.ErrorReport
import com.github.xadkile.p6.formula.translator.antlr.FormulaLexer
import com.github.xadkile.p6.formula.translator.antlr.FormulaParser
import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream
import org.antlr.v4.runtime.tree.ParseTree

/**
 * translate worksheet formula (=...) to Python
 */
class PythonFormulaTranslator : FormulaTranslator {

    companion object {
        private val scriptTranslator = ScriptFormulaTranslator()
    }
    override fun translate(formula: String): Result<String, ErrorReport> {
        val scriptRs = scriptTranslator.translate(formula)
        if(scriptRs is Ok){
            return scriptRs
        }else{
            val charStream = CharStreams.fromString(formula)
            val lexer = FormulaLexer(charStream)
            val tokenStream  = CommonTokenStream(lexer)
            val parser = FormulaParser(tokenStream)
            val tree: ParseTree = parser.formula()
            val visitor = PythonFormularVisitor()
            val out=visitor.visit(tree)
            return Ok(out)
        }
    }
}
