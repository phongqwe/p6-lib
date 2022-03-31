package com.emeraldblast.p6.formula.translator

/**
 * Map formula syntax to the equivalence in Python
 */
object PythonMapper: FormulaMapper {
    /**
     * Format bare address to legal address. eg: A1 -> @A1
     */
    override fun formatAddress(rangeAddress:String):String{
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

    override fun getWorkbook(workbookName: String): String {
        return "getWorkbook(\"${workbookName}\")"
    }
    override fun getWorkbook(index: Int): String {
        return "getWorkbook(${index})"
    }
}
