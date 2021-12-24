package com.github.xadkile.p6.formula.translator

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import com.github.xadkile.p6.formula.translator.exception.FailToParseFormulaException
import java.util.regex.Pattern

class ScriptFormulaTranslator : FormulaTranslator {

    val codePattern = Pattern.compile("=SCRIPT\\(.*\\)")

    override fun translate(formula: String): Result<String, Exception> {
        if(codePattern.matcher(formula).matches()){
            val rt = formula.substring(8,formula.length-1)
            return Ok(rt)
        }else {
            return Err(FailToParseFormulaException())
        }
    }
}
