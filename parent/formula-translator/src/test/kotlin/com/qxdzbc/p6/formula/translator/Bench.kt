package com.qxdzbc.p6.formula.translator

import org.antlr.v4.gui.TreeViewer
import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream
import org.antlr.v4.runtime.tree.ParseTree
import org.junit.jupiter.api.Test
import java.util.regex.Pattern
import javax.swing.JFrame
import javax.swing.JPanel
import kotlin.test.assertFalse
import kotlin.test.assertTrue


class Bench {
    @Test
    fun b(){
        val strPattern = Pattern.compile("\".*\"", Pattern.CASE_INSENSITIVE or Pattern.DOTALL or Pattern.MULTILINE or Pattern.UNICODE_CASE or Pattern.UNICODE_CHARACTER_CLASS or Pattern.UNIX_LINES)
        assertTrue(strPattern.matcher("\"abc\"").matches())
    }

}
