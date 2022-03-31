package com.emeraldblast.p6.formula.translator

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import com.emeraldblast.p6.common.exception.error.ErrorReport
import com.emeraldblast.p6.formula.translator.errors.TranslatorErrors
import java.util.regex.Pattern

/**
 * A special translator to handle Python script wrapped inside SCRIPT function
 */
class ScriptFormulaTranslator : FormulaTranslator {
    // TODO add pattern to match heading and trailing white space + new line
    companion object{
        val codePattern = Pattern.compile("\\s*=\\s*SCRIPT\\s*\\(.*\\)\\s*",Pattern.CASE_INSENSITIVE or Pattern.DOTALL or Pattern.MULTILINE or Pattern.UNICODE_CASE or Pattern.UNICODE_CHARACTER_CLASS or Pattern.UNIX_LINES)
    }

    override fun translate(formula: String): Result<String, ErrorReport> {
        val isMatch = codePattern.matcher(formula.uppercase()).matches()
        if(isMatch){
            val trimmed = formula.trim()
            var startIndex = 0
            for (c in trimmed){
                startIndex+=1
                if (c=='('){
                    break
                }
            }
            val rt = trimmed.substring(startIndex,trimmed.length-1)
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
