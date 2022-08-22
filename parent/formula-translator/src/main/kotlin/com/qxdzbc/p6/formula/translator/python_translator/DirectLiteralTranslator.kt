package com.qxdzbc.p6.formula.translator.python_translator

import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import com.qxdzbc.p6.common.exception.error.ErrorReport
import java.util.regex.Pattern

/**
 * Directly translate the non-formula input into either Python string or Python number that can be used directly in Python code.
 * 123 -> "123"
 * abc -> "\"abc\""
 * "abc" -> "\"abc\""
 */
class DirectLiteralTranslator: StrFormulaTranslator {
    companion object{
        private val strPattern = Pattern.compile("^\".*\"$", Pattern.CASE_INSENSITIVE or Pattern.DOTALL or Pattern.MULTILINE or Pattern.UNICODE_CASE or Pattern.UNICODE_CHARACTER_CLASS or Pattern.UNIX_LINES)
    }

    override fun translate(formula: String): Result<String, ErrorReport> {
        val i:Double? = formula.toDoubleOrNull()
        if(i!=null){
            return Ok(formula)
        }else{
            // r""" a\" """[1:-1]
            val isStringLiteral =
                strPattern.matcher(formula).matches()
            if (isStringLiteral){
                // only append 2 double quote
                return Ok("\"\"$formula\"\"")
            }else{
                // add 3 double quote
                return Ok("\"\"\"$formula\"\"\"")
            }
        }
    }
}
