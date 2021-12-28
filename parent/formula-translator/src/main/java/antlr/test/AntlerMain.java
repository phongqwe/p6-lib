package antlr.test;

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.*;

public class AntlerMain {
    public static void main(String[] args) throws Exception{
        CharStream input = CharStreams.fromString("{11,22,33}");
        ArrayInitLexer lexer = new ArrayInitLexer( input);
        CommonTokenStream tokenStream = new CommonTokenStream(lexer);
        ArrayInitParser parser = new ArrayInitParser(tokenStream);
        ParseTree tree = parser.init();
        ParseTreeWalker walker = new ParseTreeWalker();
        walker.walk(new ShortToUnicodeString(), tree);

        System.out.println();
        System.out.println("zzzz");



    }

    static class ShortToUnicodeString extends ArrayInitBaseListener{
        @Override
        public void enterInit(ArrayInitParser.InitContext ctx) {
            System.out.print("<<\"");

        }

        @Override
        public void exitInit(ArrayInitParser.InitContext ctx) {
            System.out.print("\">>");
        }

        @Override
        public void enterValue(ArrayInitParser.ValueContext ctx) {
            int value = Integer.valueOf(ctx.INT().getText());
            System.out.printf("\\u%04x", value);
        }
    }
}
