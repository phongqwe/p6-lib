package com.github.xadkile.p6.formula.translator

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import com.github.xadkile.p6.exception.lib.error.ErrorReport
import com.github.xadkile.p6.formula.translator.errors.TranslatorErrors
import java.util.regex.Pattern

/**
 * A special translator to handle Python script wrapped inside SCRIPT function
 */
class ScriptFormulaTranslator : FormulaTranslator {
    // TODO add pattern to match heading and trailing white space + new line
    val codePattern = Pattern.compile("=SCRIPT\\(.*\\)",Pattern.CASE_INSENSITIVE or Pattern.DOTALL or Pattern.MULTILINE)

    override fun translate(formula: String): Result<String, ErrorReport> {
        val z = codePattern.matcher(formula.uppercase())
        val isMatch = codePattern.matcher(formula.uppercase()).matches()
        if(isMatch){
            val rt = formula.substring(8,formula.length-1)
            return Ok(rt.trim())
        }else {
            val report = ErrorReport(
                header = TranslatorErrors.NotAScriptCall,
                data = TranslatorErrors.NotAScriptCall.Data(formula)
            )
            return Err(report)
        }
    }
}
