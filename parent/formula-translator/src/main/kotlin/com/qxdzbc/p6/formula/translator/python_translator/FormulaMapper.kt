package com.qxdzbc.p6.formula.translator.python_translator

/**
 * Map formula syntax to the equivalence in some output syntax
 */
interface FormulaMapper {
    fun formatAddress(rangeAddress:String):String
    fun getSheet(sheetName:String):String
    fun getRange(rangeAddress: String):String
    fun getCell(cellAddress:String):String
    fun getWorkbook(workbookName:String):String
    fun getWorkbook(index: Int): String
}
