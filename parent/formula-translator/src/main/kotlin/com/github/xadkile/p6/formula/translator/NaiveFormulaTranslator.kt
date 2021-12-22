package com.github.xadkile.p6.formula.translator

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.map
import com.github.xadkile.p6.formula.translator.exception.FormulaParseException
import com.github.xadkile.p6.formula.translator.static.Py
import java.util.regex.Pattern

class NaiveFormulaTranslator : FormulaTranslator {
    companion object {
        val sumPattern = Pattern.compile("SUM\\([A-Za-z]+[1-9][0-9]*:?([A-Za-z]+[1-9][0-9]*)*\\)")

        val addressPattern = Pattern.compile("[A-Za-z]+[1-9][0-9]*:?([A-Za-z]+[1-9][0-9]*)")
    }
    override fun translate(formula: String): Result<String,Exception> {
        val rangeAddress = this.extractAddress(formula)
        val rt = rangeAddress.map {
            "${Py.WorksheetFunctions}.SUM(getRange(\"@$it\"))"
        }
        return rt
    }

    private fun extractAddress(fo:String):Result<String,Exception>{
        val o2 = addressPattern.matcher(fo)
        val found = o2.find()
        if(found){
            return Ok(fo.substring(o2.start(0),o2.end(0)))
        }else{
            return Err(FormulaParseException())
        }
    }
}
