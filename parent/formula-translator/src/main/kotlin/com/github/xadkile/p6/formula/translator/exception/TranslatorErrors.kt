package com.github.xadkile.p6.formula.translator.exception

import com.github.xadkile.p6.exception.error.ErrorHeader

object TranslatorErrors {
    object FailToParseFormulaErr : ErrorHeader("FailToParseFormulaErr".hashCode(),"fail to parse a formula"){
        class Data(val formula:String, val errorDetail:String)
    }
}
