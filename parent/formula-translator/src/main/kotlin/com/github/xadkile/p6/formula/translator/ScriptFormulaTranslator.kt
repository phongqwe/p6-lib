package com.github.xadkile.p6.formula.translator

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import com.github.xadkile.p6.exception.error.ErrorReport
import com.github.xadkile.p6.formula.translator.exception.FailToParseFormulaException
import com.github.xadkile.p6.formula.translator.exception.TranslatorErrors
import java.util.regex.Pattern

class ScriptFormulaTranslator : FormulaTranslator {

    val codePattern = Pattern.compile("=SCRIPT\\(.*\\)")

    override fun translate(formula: String): Result<String, ErrorReport> {
        if(codePattern.matcher(formula).matches()){
            val rt = formula.substring(8,formula.length-1)
            return Ok(rt)
        }else {
            val report = ErrorReport(
                header = TranslatorErrors.FailToParseFormulaErr,
                data = TranslatorErrors.FailToParseFormulaErr.Data(formula,"")
            )
            return Err(report)
        }
    }
}
