package com.github.xadkile.p6.formula.translator


object PythonMapper:FormulaMapper {
    override fun rangeAddress(rangeAddress:String):String{
        return  "\"@${rangeAddress}\""
    }
    override fun getSheet(sheetName:String):String{
        return "getSheet(\"${sheetName}\")"
    }

    override fun getRange(rangeAddress: String): String {
        return "getRange(${rangeAddress})"
    }

    override fun getCell(cellAddress: String): String {
        return "cell(${cellAddress})"
    }
}
