package com.github.xadkile.p6.formula.translator

/**
 * Map certain formula syntax to the equivalence in python
 */
object PythonMapper {
    fun mapRangeAddress(rangeAddr:String):String{
        return  "@${rangeAddr}"
    }
    fun mapGetSheet(sheetName:String):String{
        return "getSheet(\"${sheetName}\")"
    }

//    fun getCellFromSheet(sheetName: String,cellAddr:String):String{
//        return mapGetSheet()
//    }
}
