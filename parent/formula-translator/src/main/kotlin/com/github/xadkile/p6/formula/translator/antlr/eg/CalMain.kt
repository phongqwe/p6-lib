package com.github.xadkile.p6.formula.translator.antlr.eg

import com.github.xadkile.p6.formula.translator.antlr.eg.CalParser.*
import org.antlr.v4.runtime.CharStream
import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream
import org.antlr.v4.runtime.TokenStream
import org.antlr.v4.runtime.tree.ParseTree

object CalMain {
    @Throws(Exception::class)
    @JvmStatic
    fun main(args: Array<String>) {
        val input = """
            193
            a=5
            b=6
            a+b*2
            (1+2)*3

            """.trimIndent()
        val charStream: CharStream = CharStreams.fromString(input)
        val lexer = CalLexer(charStream)
        val tokenStream: TokenStream = CommonTokenStream(lexer)
        val parser = CalParser(tokenStream)
        val tree: ParseTree = parser.prog()
        val visitor = EvalVisitor()
        visitor.visit(tree)
    }

    internal class MyListener : CalBaseListener() {
        override fun enterProg(ctx: CalParser.ProgContext?) {
            super.enterProg(ctx)
        }
    }

    internal class EvalVisitor : CalBaseVisitor<Int?>() {
        var memory: MutableMap<String, Int> = HashMap()
        override fun visitClear(ctx: CalParser.ClearContext?): Int {
            memory.clear()
            return 0
        }

        override fun visitPrintExpr(ctx: CalParser.PrintExprContext): Int {
            val value: Int? = this.visit(ctx.expr())
            println(value)
            return 0
        }

        /**
         * ID '=' expr NEWLINE
         */
        override fun visitAssign(ctx: CalParser.AssignContext): Int {
            val id = ctx.ID().text
            val value: Int? = this.visit(ctx.expr())
            memory[id] = value ?: 0
            return value ?: 0
        }

        override fun visitBlank(ctx: CalParser.BlankContext?): Int {
            return super.visitBlank(ctx) ?: 0
        }

        override fun visitParens(ctx: ParensContext): Int {
            return this.visit(ctx.expr()) ?:0
        }

        override fun visitMulDiv(ctx: MulDivContext): Int {
            val left: Int = visit(ctx.expr(0)) ?:0
            val right: Int = this.visit(ctx.expr(1)) ?:0
            return if (ctx.op.type == MUL) {
                left * right
            } else {
                left / right
            }
        }

        override fun visitAddSub(ctx: AddSubContext): Int {
            val left: Int = this.visit(ctx.expr(0)) ?:0
            val right: Int = this.visit(ctx.expr(1))?:0
            return if (ctx.op.type == ADD) {
                left + right
            } else {
                left - right
            }
        }

        // ID
        override fun visitId(ctx: IdContext): Int? {
            val id = ctx.ID().text
            return if (memory.containsKey(id)) {
                memory[id]
            } else {
                0
            }
        }

        // INT
        override fun visitInt(ctx: IntContext): Int {
            return Integer.valueOf(ctx.INT().text)
        }
    }
}
