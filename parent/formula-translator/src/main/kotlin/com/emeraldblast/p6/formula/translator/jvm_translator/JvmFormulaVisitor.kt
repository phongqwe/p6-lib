package com.emeraldblast.p6.formula.translator.jvm_translator

import com.emeraldblast.p6.formula.FunctionMap
import com.emeraldblast.p6.formula.execution_unit.ExUnit
import com.emeraldblast.p6.formula.translator.antlr.FormulaBaseVisitor
import com.emeraldblast.p6.formula.translator.antlr.FormulaParser

class JvmFormulaVisitor(
    private val functionMap: FunctionMap
) : FormulaBaseVisitor<ExUnit>() {
    override fun visitZFormula(ctx: FormulaParser.ZFormulaContext?): ExUnit {
        val rt = ctx?.expr()?.let { this.visit(it) } ?: ExUnit.Nothing
        return rt
    }

    override fun visitFunCall(ctx: FormulaParser.FunCallContext?): ExUnit {
        val name = "${visit(ctx?.functionCall()?.functionName())}"
        val args: List<ExUnit?> = ctx?.functionCall()?.expr()?.map { visit(it) } ?: emptyList()
        val rt = ExUnit.Func(
            funcName = name,
            args = args.filterNotNull(),
            functionMap = functionMap
        )
        return rt
    }

    override fun visitLiteral(ctx: FormulaParser.LiteralContext?): ExUnit {
        val floatNode = ctx?.lit()?.FLOAT_NUMBER()
        val intNode = ctx?.lit()?.INT()
        val textNode = ctx?.lit()?.STRING()
        val boolNode = ctx?.lit()?.BOOLEAN()

        if (boolNode != null) {
            when (boolNode.text) {
                "TRUE" -> return ExUnit.TRUE
                "FALSE" -> return ExUnit.FALSE
            }
        }

        if (floatNode != null) {
            val doubleNum = floatNode.text.toDoubleOrNull()
            if (doubleNum != null) {
                return ExUnit.DoubleNum(doubleNum)
            }
        }
        if (intNode != null) {
            val i = intNode.text.toIntOrNull()
            if (i != null) {
                return ExUnit.IntNum(i)
            }
        }

        if (textNode != null) {
            val ot = textNode.text
            val t = ot?.substring(1, ot.length - 1)
            if (t != null) {
                return ExUnit.Text(t)
            }
        }
        return ExUnit.Nothing
    }

    override fun visitUnSubExpr(ctx: FormulaParser.UnSubExprContext?): ExUnit {
        val op = ctx?.op
        when (op?.type) {
            FormulaParser.SUB -> {
                val exUnit = ctx.expr()?.let { this.visit(it) }
                if (exUnit != null) {
                    val rt = ExUnit.UnarySubtract(exUnit)
                    return rt
                } else {
                    return ExUnit.Nothing
                }
            }
        }
        return ExUnit.Nothing
    }

    override fun visitParenExpr(ctx: FormulaParser.ParenExprContext?): ExUnit {
        return this.visit(ctx?.expr())
    }

    override fun visitPowExpr(ctx: FormulaParser.PowExprContext): ExUnit {
        val expr0 = this.visit(ctx.expr(0))
        val expr1 = this.visit(ctx.expr(1))
        return ExUnit.PowerBy(expr0, expr1)
    }

    override fun visitMulDivModExpr(ctx: FormulaParser.MulDivModExprContext): ExUnit {
        val expr0 = this.visit(ctx.expr(0))
        val expr1 = this.visit(ctx.expr(1))
        val op = ctx.op
        when (op?.type) {
            FormulaParser.MUL -> {
                return ExUnit.Mul(expr0, expr1)
            }
            FormulaParser.DIV -> {
                return ExUnit.Div(expr0, expr1)
            }
            else -> return ExUnit.Nothing
        }
    }

    override fun visitAddSubExpr(ctx: FormulaParser.AddSubExprContext): ExUnit {
        val expr0 = this.visit(ctx.expr(0))
        val expr1 = this.visit(ctx.expr(1))
        val op = ctx.op
        when (op?.type) {
            FormulaParser.ADD -> {
                return ExUnit.Add(expr0, expr1)
            }
            FormulaParser.SUB -> {
                return ExUnit.Sub(expr0, expr1)
            }
            else -> return ExUnit.Nothing
        }
    }

//    override fun visitSheetRangeAddrExpr(ctx: FormulaParser.SheetRangeAddrExprContext): ExUnit {
//        val sheetName = this.extractSheetName(ctx.SHEET_PREFIX()?.text)
//        val getSheet = if(sheetName.isEmpty()){
//            ""
//        }else{
//            PythonFormularVisitor.mapper.getSheet(sheetName)+"."
//        }
//        val rangeObj = this.visit(ctx.rangeAddress())
//        return "${getSheet}${rangeObj}"
//    }
//
//    override fun visitFunctionCall(ctx: FormulaParser.FunctionCallContext): String {
//        val functionName = this.visit(ctx.functionName())
//        val args = ctx.expr()?.map {
//            this.visit(it)
//        }?.joinToString(", ") ?: emptyList<String>()
//        return "${PythonFormularVisitor.functionLib}.${functionName}(${args})"
//    }
//
//
//    override fun visitFunctionName(ctx: FormulaParser.FunctionNameContext): String {
//        return ctx.text
//    }
//
//    override fun visitPairCellAddress(ctx: FormulaParser.PairCellAddressContext): String {
//        val cell0 = ctx.cellAddress(0).text
//        val cell1 = ctx.cellAddress(1).text
//        val rangeAddress = PythonFormularVisitor.mapper.formatAddress("${cell0}:${cell1}")
//        return PythonFormularVisitor.mapper.getRange(rangeAddress)
//    }
//
//    override fun visitOneCellAddress(ctx: FormulaParser.OneCellAddressContext): String {
//        return PythonFormularVisitor.mapper.getCell(PythonFormularVisitor.mapper.formatAddress(ctx.cellAddress().text))+".value"
//    }
//
//    override fun visitColAddress(ctx: FormulaParser.ColAddressContext): String {
//        return PythonFormularVisitor.mapper.getRange(PythonFormularVisitor.mapper.formatAddress(ctx.text))
//    }
//
//    override fun visitRowAddress(ctx: FormulaParser.RowAddressContext): String {
//        return PythonFormularVisitor.mapper.getRange(PythonFormularVisitor.mapper.formatAddress(ctx.text))
//    }
//
//    override fun visitParensAddress(ctx: FormulaParser.ParensAddressContext): String {
//        return "(${this.visit(ctx.rangeAddress())})"
//    }
//
//    override fun visitCellAddress(ctx: FormulaParser.CellAddressContext?): String {
//        return super.visitCellAddress(ctx)
//    }
//
//    override fun visitLit(ctx: FormulaParser.LitContext): String {
//        return ctx.text
//    }
//
    /**
     * Extract sheet name from this format:
     * !'SheetName' -> SheetName
     * !SheetName -> SheetName
     */
    private fun extractSheetName(rawSheetName: String?): String {
        if(rawSheetName==null){
            return ""
        }
        if (rawSheetName.isBlank() || rawSheetName.isEmpty()) {
            return ""
        } else {
            val rt = if (rawSheetName.startsWith('\'')) {
                rawSheetName.substring(1, rawSheetName.length - 2)
            } else {
                rawSheetName.substring(0, rawSheetName.length - 1)
            }
            return rt
        }
    }
}
