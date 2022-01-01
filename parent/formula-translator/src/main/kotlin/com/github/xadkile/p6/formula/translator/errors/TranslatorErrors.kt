package com.github.xadkile.p6.formula.translator.errors

import com.github.xadkile.p6.exception.lib.error.ErrorHeader

object TranslatorErrors {
    private const val prefix = "Translator Error "
    object FailToParseFormulaErr : ErrorHeader("${prefix}1","fail to parse a formula"){
        class Data(val formula:String, val errorDetail:String)
    }
}
