package com.github.xadkile.p6.formula.translator

import com.github.xadkile.p6.formula.translator.antlr.FormulaBaseVisitor
import com.github.xadkile.p6.formula.translator.antlr.FormulaParser

/**
 * This class translates formula to Python
 */
class PythonFormularVisitor : FormulaBaseVisitor<String>() {
    companion object {
        val functionLib:String = PythonLangElements.wsfunctionPrefix
        val mapper:FormulaMapper = PythonMapper
    }

    override fun visitFormula(ctx: FormulaParser.FormulaContext?): String {
        val rt: String = ctx?.expr()?.let { this.visit(it) } ?: ""
        return rt
    }

    override fun visitFunCall(ctx: FormulaParser.FunCallContext?): String {
        val name = "${functionLib}.${visit(ctx?.functionCall()?.functionName())}"
        val args: List<String> = ctx?.functionCall()?.expr()?.map { visit(it) } ?: emptyList()
        return "$name(${args.joinToString(",")})"
    }

    override fun visitParenExpr(ctx: FormulaParser.ParenExprContext?): String {
        return "(${this.visit(ctx?.expr())})"
    }

    override fun visitLiteral(ctx: FormulaParser.LiteralContext?): String {
        return ctx?.text ?: ""
    }

    override fun visitUnExpr(ctx: FormulaParser.UnExprContext?): String {
        val expr: String = ctx?.expr()?.let { this.visit(it) } ?: ""
        return "${ctx?.op?.text ?: ""}${expr}"
    }

    override fun visitPowExpr(ctx: FormulaParser.PowExprContext): String {
        val expr0 = this.visit(ctx.expr(0))
        val expr1 = this.visit(ctx.expr(1))
        return "$expr0**${expr1}"
    }

    override fun visitMulDivModExpr(ctx: FormulaParser.MulDivModExprContext): String {
        val expr0 = this.visit(ctx.expr(0))
        val op = ctx.op.text
        val expr1 = this.visit(ctx.expr(1))
        return "$expr0${op}${expr1}"
    }

    override fun visitAddSubExpr(ctx: FormulaParser.AddSubExprContext): String {
        val expr0 = this.visit(ctx.expr(0))
        val op = ctx.op.text
        val expr1 = this.visit(ctx.expr(1))
        return "$expr0${op}${expr1}"
    }

    override fun visitSheetRangeAddrExpr(ctx: FormulaParser.SheetRangeAddrExprContext): String {
        val sheetName = this.extractSheetName(ctx.SHEET_PREFIX()?.text)
        val getSheet = if(sheetName.isEmpty()){
            ""
        }else{
            mapper.getSheet(sheetName)+"."
        }
        val rangeObj = this.visit(ctx.rangeAddress())
        return "${getSheet}${rangeObj}"
    }

    override fun visitFunctionCall(ctx: FormulaParser.FunctionCallContext): String {
        val functionName = this.visit(ctx.functionName())
        val args = ctx.expr()?.map {
            this.visit(it)
        }?.joinToString(", ") ?: emptyList<String>()
        return "${functionLib}.${functionName}(${args})"
    }

    override fun visitFunctionName(ctx: FormulaParser.FunctionNameContext): String {
        return ctx.text
    }

    override fun visitPairCellAddress(ctx: FormulaParser.PairCellAddressContext): String {
        val cell0 = ctx.cellAddress(0).text
        val cell1 = ctx.cellAddress(1).text
        val rangeAddress = mapper.rangeAddress("${cell0}:${cell1}")
        return mapper.getRange(rangeAddress)
    }

    override fun visitOneCellAddress(ctx: FormulaParser.OneCellAddressContext): String {
        return mapper.getCell(mapper.rangeAddress(ctx.cellAddress().text))+".value"
    }

    override fun visitColAddress(ctx: FormulaParser.ColAddressContext): String {
        return mapper.getRange(mapper.rangeAddress(ctx.text))
    }

    override fun visitRowAddress(ctx: FormulaParser.RowAddressContext): String {
        return mapper.getRange(mapper.rangeAddress(ctx.text))
    }

    override fun visitParensAddress(ctx: FormulaParser.ParensAddressContext): String {
        return "(${this.visit(ctx.rangeAddress())})"
    }

    override fun visitCellAddress(ctx: FormulaParser.CellAddressContext?): String {
        return super.visitCellAddress(ctx)
    }

    override fun visitLit(ctx: FormulaParser.LitContext): String {
        return ctx.text
    }

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
