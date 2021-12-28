package antlr.test.cal;// Generated from Cal.g4 by ANTLR 4.9.2
import org.antlr.v4.runtime.tree.ParseTreeListener;

/**
 * This interface defines a complete listener for a parse tree produced by
 * {@link CalParser}.
 */
public interface CalListener extends ParseTreeListener {
	/**
	 * Enter a parse tree produced by {@link CalParser#prog}.
	 * @param ctx the parse tree
	 */
	void enterProg(CalParser.ProgContext ctx);
	/**
	 * Exit a parse tree produced by {@link CalParser#prog}.
	 * @param ctx the parse tree
	 */
	void exitProg(CalParser.ProgContext ctx);
	/**
	 * Enter a parse tree produced by the {@code printExpr}
	 * labeled alternative in {@link CalParser#stat}.
	 * @param ctx the parse tree
	 */
	void enterPrintExpr(CalParser.PrintExprContext ctx);
	/**
	 * Exit a parse tree produced by the {@code printExpr}
	 * labeled alternative in {@link CalParser#stat}.
	 * @param ctx the parse tree
	 */
	void exitPrintExpr(CalParser.PrintExprContext ctx);
	/**
	 * Enter a parse tree produced by the {@code assign}
	 * labeled alternative in {@link CalParser#stat}.
	 * @param ctx the parse tree
	 */
	void enterAssign(CalParser.AssignContext ctx);
	/**
	 * Exit a parse tree produced by the {@code assign}
	 * labeled alternative in {@link CalParser#stat}.
	 * @param ctx the parse tree
	 */
	void exitAssign(CalParser.AssignContext ctx);
	/**
	 * Enter a parse tree produced by the {@code blank}
	 * labeled alternative in {@link CalParser#stat}.
	 * @param ctx the parse tree
	 */
	void enterBlank(CalParser.BlankContext ctx);
	/**
	 * Exit a parse tree produced by the {@code blank}
	 * labeled alternative in {@link CalParser#stat}.
	 * @param ctx the parse tree
	 */
	void exitBlank(CalParser.BlankContext ctx);
	/**
	 * Enter a parse tree produced by the {@code parens}
	 * labeled alternative in {@link CalParser#expr}.
	 * @param ctx the parse tree
	 */
	void enterParens(CalParser.ParensContext ctx);
	/**
	 * Exit a parse tree produced by the {@code parens}
	 * labeled alternative in {@link CalParser#expr}.
	 * @param ctx the parse tree
	 */
	void exitParens(CalParser.ParensContext ctx);
	/**
	 * Enter a parse tree produced by the {@code MulDiv}
	 * labeled alternative in {@link CalParser#expr}.
	 * @param ctx the parse tree
	 */
	void enterMulDiv(CalParser.MulDivContext ctx);
	/**
	 * Exit a parse tree produced by the {@code MulDiv}
	 * labeled alternative in {@link CalParser#expr}.
	 * @param ctx the parse tree
	 */
	void exitMulDiv(CalParser.MulDivContext ctx);
	/**
	 * Enter a parse tree produced by the {@code AddSub}
	 * labeled alternative in {@link CalParser#expr}.
	 * @param ctx the parse tree
	 */
	void enterAddSub(CalParser.AddSubContext ctx);
	/**
	 * Exit a parse tree produced by the {@code AddSub}
	 * labeled alternative in {@link CalParser#expr}.
	 * @param ctx the parse tree
	 */
	void exitAddSub(CalParser.AddSubContext ctx);
	/**
	 * Enter a parse tree produced by the {@code id}
	 * labeled alternative in {@link CalParser#expr}.
	 * @param ctx the parse tree
	 */
	void enterId(CalParser.IdContext ctx);
	/**
	 * Exit a parse tree produced by the {@code id}
	 * labeled alternative in {@link CalParser#expr}.
	 * @param ctx the parse tree
	 */
	void exitId(CalParser.IdContext ctx);
	/**
	 * Enter a parse tree produced by the {@code int}
	 * labeled alternative in {@link CalParser#expr}.
	 * @param ctx the parse tree
	 */
	void enterInt(CalParser.IntContext ctx);
	/**
	 * Exit a parse tree produced by the {@code int}
	 * labeled alternative in {@link CalParser#expr}.
	 * @param ctx the parse tree
	 */
	void exitInt(CalParser.IntContext ctx);
}
