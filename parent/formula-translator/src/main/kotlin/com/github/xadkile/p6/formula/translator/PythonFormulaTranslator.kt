package com.github.xadkile.p6.formula.translator

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import com.github.xadkile.p6.exception.lib.error.ErrorReport
import com.github.xadkile.p6.formula.translator.antlr.FormulaLexer
import com.github.xadkile.p6.formula.translator.antlr.FormulaParser
import com.github.xadkile.p6.formula.translator.errors.TranslatorErrors
import org.antlr.v4.runtime.*
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
            var parserErrorData:TranslatorErrors.ParserErr.Data? = null
            var lexerErrData:TranslatorErrors.LexerErr.Data? = null

            val charStream: CharStream = CharStreams.fromString(formula)
            val lexer = FormulaLexer(charStream)
            /////////
            lexer.removeErrorListeners()
            lexer.addErrorListener( object : BaseErrorListener(){
                override fun syntaxError(
                    recognizer: Recognizer<*, *>?,
                    offendingSymbol: Any?,
                    line: Int,
                    charPositionInLine: Int,
                    msg: String?,
                    e: RecognitionException?,
                ) {
                    lexerErrData = TranslatorErrors.LexerErr.Data(
                        formula = formula,
                        recognizer = recognizer,
                        offendingSymbol = offendingSymbol,
                        line = line,
                        charPositionInLine = charPositionInLine,
                        msg =msg,
                        recognitionException = e
                    )
                }
            })

            val tokenStream  = CommonTokenStream(lexer)
            val parser = FormulaParser(tokenStream)

            parser.removeErrorListeners()
            parser.addErrorListener( object : BaseErrorListener(){
                override fun syntaxError(
                    recognizer: Recognizer<*, *>?,
                    offendingSymbol: Any?,
                    line: Int,
                    charPositionInLine: Int,
                    msg: String?,
                    e: RecognitionException?,
                ) {
                    parserErrorData = TranslatorErrors.ParserErr.Data(
                        formula = formula,
                        recognizer = recognizer,
                        offendingSymbol = offendingSymbol,
                        line = line,
                        charPositionInLine = charPositionInLine,
                        msg =msg,
                        recognitionException = e
                    )
                }
            })
            val tree: ParseTree = parser.formula()

            if(parserErrorData!=null || lexerErrData!=null){
                return Err(ErrorReport(
                    header= TranslatorErrors.TranslatingErr,
                    data = TranslatorErrors.TranslatingErr.Data(
                        lexerErr = lexerErrData,
                        parserErr = parserErrorData
                    ),
                    loc =  this.javaClass.canonicalName
                ))
            }
            val visitor = PythonFormularVisitor()
            val out=visitor.visit(tree)
            return Ok(out)
        }
    }
}
