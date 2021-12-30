package com.github.xadkile.p6.formula.translator

import com.github.xadkile.p6.formula.translator.antlr.FormulaBaseVisitor
import com.github.xadkile.p6.formula.translator.antlr.FormulaParser

/**
 * This class translates formula to Python
 */
class PythonFormularVisitor : FormulaBaseVisitor<String>(){
    companion object{
        val flib = LanguageConst.wsfunction
    }

    override fun visitFormula(ctx: FormulaParser.FormulaContext?): String {
        val rt:String = ctx?.expr().let { this.visit(it) } ?: ""
        return rt
    }

    override fun visitFunCall(ctx: FormulaParser.FunCallContext): String {
        val name = "${flib}.${visit(ctx.functionCall().functionName())}"
        val args:List<String> = ctx.functionCall().expr().map{visit(it)}
        return "$name(${args.joinToString(",")})"
    }

    override fun visitParenExpr(ctx: FormulaParser.ParenExprContext): String {
        return "(${this.visit(ctx.expr())})"
    }

    override fun visitLiteral(ctx: FormulaParser.LiteralContext): String {
        return ctx.text
    }

    override fun visitUnExpr(ctx: FormulaParser.UnExprContext): String {
        val expr:String = this.visit(ctx.expr())
        return "${ctx.op.text}${expr}"
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
        val sheetName = this.extractSheetName(ctx.sheetRangeAddress().SHEET_PREFIX().text)
        val rangeAddress = this.visit(ctx.sheetRangeAddress().rangeAddress())
        return "getSheet(\"${sheetName}\").range(\"@${rangeAddress}\").value"
    }

    override fun visitFunctionCall(ctx: FormulaParser.FunctionCallContext): String {
        val functionName = this.visit(ctx.functionName())
        val args = ctx.expr().map {
            this.visit(it)
        }.joinToString(", ")
        return "${flib}.${functionName}(${args})"
    }

    override fun visitFunctionName(ctx: FormulaParser.FunctionNameContext): String {
        return ctx.text
    }


    override fun visitSheetRangeAddress(ctx: FormulaParser.SheetRangeAddressContext?): String {
        return super.visitSheetRangeAddress(ctx)
    }

    private fun extractSheetName(rawSheetName:String):String{
        if(rawSheetName.isBlank() || rawSheetName.isEmpty()){
            return ""
        }else{
            if(rawSheetName.startsWith('\'')){
                return rawSheetName.substring(1,rawSheetName.length-1)
            }else{
                return rawSheetName.substring(0,rawSheetName.length-1)
            }
        }
    }


}
