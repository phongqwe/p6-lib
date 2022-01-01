package com.github.xadkile.p6.formula.translator

import com.github.xadkile.p6.formula.translator.antlr.FormulaLexer
import com.github.xadkile.p6.formula.translator.antlr.FormulaParser
import org.antlr.v4.gui.TreeViewer
import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream
import org.antlr.v4.runtime.tree.ParseTree
import org.junit.jupiter.api.Test
import javax.swing.JFrame
import javax.swing.JPanel


class Bench {
    @Test
    fun b(){
        var input="\"abc\""
        input="=F1(1+1)"
        val charStream = CharStreams.fromString(input)
        val lexer = FormulaLexer(charStream)
        val zzz = CommonTokenStream(lexer)
        val parser = FormulaParser(zzz)
        val tree:ParseTree = parser.formula()
        val frame = JFrame("Antlr AST")
        val panel = JPanel()
        val treeViewer = TreeViewer(parser.ruleNames.toList(), tree)

        treeViewer.setScale(1.5); // Scale a little
        panel.add(treeViewer);
        frame.add(panel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
//        println(tree.toStringTree())
    }

}
