package com.github.xadkile.p6.formula.translator

/**
 * Map certain formula syntax to the equivalence in python
 */
interface FormulaMapper {
    fun rangeAddress(rangeAddress:String):String
    fun getSheet(sheetName:String):String
    fun getRange(rangeAddress: String):String
    fun getCell(cellAddress:String):String
    fun getWorkbook(workbookName:String):String
    fun getWorkbook(index: Int): String
}
