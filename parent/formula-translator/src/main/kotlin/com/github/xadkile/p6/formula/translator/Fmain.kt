package com.github.xadkile.p6.formula.translator

import com.github.xadkile.p6.formula.translator.antlr.*
import org.antlr.v4.gui.TreeViewer
import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream
import org.antlr.v4.runtime.tree.ParseTree
import javax.swing.JFrame
import javax.swing.JPanel

fun main(args:Array<String>):Unit{
    var input="\"abc\""
    input="=F1(1)"
    val charStream = CharStreams.fromString(input)
    val lexer = FormulaLexer(charStream)
    val zzz = CommonTokenStream(lexer)
    val parser = FormulaParser(zzz)
    val tree: ParseTree = parser.formula()
    val visitor = MyVisitor()
    val out=visitor.visit(tree)
    println(out)

//    val frame = JFrame("Antlr AST")
//    val panel = JPanel()
//    val treeViewer = TreeViewer(parser.ruleNames.toList(), tree)
//    treeViewer.setScale(1.5); // Scale a little
//    panel.add(treeViewer);
//    frame.add(panel);
//    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//    frame.pack();
//    frame.setVisible(true);
}


class MyVisitor : FormulaBaseVisitor<String>(){
    override fun visitFormula(ctx: FormulaParser.FormulaContext?): String {
        val rt:String = ctx?.expr().let { this.visit(it) } ?: ""
        return rt
    }


    override fun visitFunCall(ctx: FormulaParser.FunCallContext): String {
        val name = "WSF.${visit(ctx.functionCall().functionName())}"
        val args:List<String> = ctx.functionCall().expr().map{visit(it)}
        return "$name(${args.joinToString(",")})"
    }

    override fun visitFunctionName(ctx: FormulaParser.FunctionNameContext): String {
        return ctx.text
    }

    override fun visitLiteral(ctx: FormulaParser.LiteralContext): String {
        return ctx.text
    }
}

class MyListener: FormulaBaseListener(){

}
