package com.github.xadkile.p6.formula.translator.errors

import com.github.xadkile.p6.common.exception.error.ErrorType
import org.antlr.v4.runtime.RecognitionException
import org.antlr.v4.runtime.Recognizer

object TranslatorErrors {
    private const val prefix = "Translator Error "
    object TranslatingErr: ErrorType("${prefix}0", "translating error") {
        class Data(
            val lexerErr:LexerErr.Data?,
            val parserErr:ParserErr.Data?,
        )
    }

    object ParserErr : ErrorType("${prefix}1", "parser error") {
        class Data(
            val formula: String,
            val recognizer: Recognizer<*, *>?,
            val offendingSymbol: Any?,
            val line: Int,
            val charPositionInLine: Int,
            val msg: String?,
            val recognitionException: RecognitionException?,
        )
    }
    object LexerErr : ErrorType("${prefix}2", "lexer error") {
        class Data(
            val formula: String,
            val recognizer: Recognizer<*, *>?,
            val offendingSymbol: Any?,
            val line: Int,
            val charPositionInLine: Int,
            val msg: String?,
            val recognitionException: RecognitionException?,
        )
    }

    object NotAScriptCall : ErrorType("${prefix}2","input is not a =SCRIPT() call"){
        class Data(val formula:String)
    }
}
