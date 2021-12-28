package antlr.test.cal;

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.ErrorNode;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.antlr.v4.runtime.tree.TerminalNode;

import java.util.HashMap;
import java.util.Map;

public class CalMain {
    public static void main(String[] args) throws Exception {

        String input ="193\n" + "a=5\n" + "b=6\n"+"a+b*2\n"+"(1+2)*3\n";
        CharStream charStream = CharStreams.fromString(input);
        CalLexer lexer = new CalLexer(charStream);
        TokenStream tokenStream = new CommonTokenStream(lexer);
        CalParser parser = new CalParser(tokenStream);
        ParseTree tree = parser.prog();
//        ParseTreeWalker walker = new ParseTreeWalker();
//
//        walker.walk(new MyListener(), tree);
        EvalVisitor visitor = new EvalVisitor();
        visitor.visit(tree);
    }

    static class MyListener extends CalBaseListener {

    }

    static class EvalVisitor extends CalBaseVisitor<Integer> {
        Map<String, Integer> memory = new HashMap<>();

        @Override
        public Integer visitPrintExpr(CalParser.PrintExprContext ctx) {
            int value = this.visit(ctx.expr());
            System.out.println(value);
            return 0;
        }

        /**
         * ID '=' expr NEWLINE
         */
        @Override
        public Integer visitAssign(CalParser.AssignContext ctx) {
            String id = ctx.ID().getText();
            int value = this.visit(ctx.expr());
            memory.put(id,value);
            return value;
        }

        @Override
        public Integer visitBlank(CalParser.BlankContext ctx) {
            return super.visitBlank(ctx);
        }

        @Override
        public Integer visitParens(CalParser.ParensContext ctx) {
            return this.visit(ctx.expr());
        }

        @Override
        public Integer visitMulDiv(CalParser.MulDivContext ctx) {
            int left = visit(ctx.expr(0));
            int right = this.visit(ctx.expr(1));
            if(ctx.op.getType() == CalParser.MUL){
                return left * right;
            }else{
                return left/right;
            }
        }

        @Override
        public Integer visitAddSub(CalParser.AddSubContext ctx) {
            int left = this.visit(ctx.expr(0));
            int right = this.visit(ctx.expr(1));
            if(ctx.op.getType() == CalParser.ADD){
                return left + right;
            }else{
                return left - right;
            }
        }

        // ID
        @Override
        public Integer visitId(CalParser.IdContext ctx) {
            String id = ctx.ID().getText();
            if(memory.containsKey(id)){
                return memory.get(id);
            }else{
                return 0;
            }
        }

        // INT
        @Override
        public Integer visitInt(CalParser.IntContext ctx) {
            return Integer.valueOf(ctx.INT().getText());
        }
    }

}
