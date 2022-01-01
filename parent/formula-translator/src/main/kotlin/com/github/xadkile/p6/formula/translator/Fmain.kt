package com.github.xadkile.p6.formula.translator

import com.github.xadkile.p6.formula.translator.antlr.FormulaLexer
import com.github.xadkile.p6.formula.translator.antlr.FormulaParser
import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream
import org.antlr.v4.runtime.tree.ParseTree

fun main(args:Array<String>):Unit{
    var input="\"abc\""
    input="=sum(1123,123, A1:A2,)"
    val charStream = CharStreams.fromString(input)
    val lexer = FormulaLexer(charStream)
    val zzz = CommonTokenStream(lexer)
    val parser = FormulaParser(zzz)
    val tree: ParseTree = parser.formula()
    val visitor = PythonFormularVisitor()
    val out=visitor.visit(tree)
    println(out)
}

