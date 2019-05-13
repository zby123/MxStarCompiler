// Generated from Mxstar.g4 by ANTLR 4.7.1
package Mxstar.Parser;
import org.antlr.v4.runtime.tree.ParseTreeListener;

/**
 * This interface defines a complete listener for a parse tree produced by
 * {@link MxstarParser}.
 */
public interface MxstarListener extends ParseTreeListener {
	/**
	 * Enter a parse tree produced by {@link MxstarParser#compilation_unit}.
	 * @param ctx the parse tree
	 */
	void enterCompilation_unit(MxstarParser.Compilation_unitContext ctx);
	/**
	 * Exit a parse tree produced by {@link MxstarParser#compilation_unit}.
	 * @param ctx the parse tree
	 */
	void exitCompilation_unit(MxstarParser.Compilation_unitContext ctx);
	/**
	 * Enter a parse tree produced by {@link MxstarParser#top_defs}.
	 * @param ctx the parse tree
	 */
	void enterTop_defs(MxstarParser.Top_defsContext ctx);
	/**
	 * Exit a parse tree produced by {@link MxstarParser#top_defs}.
	 * @param ctx the parse tree
	 */
	void exitTop_defs(MxstarParser.Top_defsContext ctx);
	/**
	 * Enter a parse tree produced by {@link MxstarParser#defvars}.
	 * @param ctx the parse tree
	 */
	void enterDefvars(MxstarParser.DefvarsContext ctx);
	/**
	 * Exit a parse tree produced by {@link MxstarParser#defvars}.
	 * @param ctx the parse tree
	 */
	void exitDefvars(MxstarParser.DefvarsContext ctx);
	/**
	 * Enter a parse tree produced by {@link MxstarParser#vars}.
	 * @param ctx the parse tree
	 */
	void enterVars(MxstarParser.VarsContext ctx);
	/**
	 * Exit a parse tree produced by {@link MxstarParser#vars}.
	 * @param ctx the parse tree
	 */
	void exitVars(MxstarParser.VarsContext ctx);
	/**
	 * Enter a parse tree produced by {@link MxstarParser#var}.
	 * @param ctx the parse tree
	 */
	void enterVar(MxstarParser.VarContext ctx);
	/**
	 * Exit a parse tree produced by {@link MxstarParser#var}.
	 * @param ctx the parse tree
	 */
	void exitVar(MxstarParser.VarContext ctx);
	/**
	 * Enter a parse tree produced by {@link MxstarParser#defun}.
	 * @param ctx the parse tree
	 */
	void enterDefun(MxstarParser.DefunContext ctx);
	/**
	 * Exit a parse tree produced by {@link MxstarParser#defun}.
	 * @param ctx the parse tree
	 */
	void exitDefun(MxstarParser.DefunContext ctx);
	/**
	 * Enter a parse tree produced by {@link MxstarParser#defclass}.
	 * @param ctx the parse tree
	 */
	void enterDefclass(MxstarParser.DefclassContext ctx);
	/**
	 * Exit a parse tree produced by {@link MxstarParser#defclass}.
	 * @param ctx the parse tree
	 */
	void exitDefclass(MxstarParser.DefclassContext ctx);
	/**
	 * Enter a parse tree produced by {@link MxstarParser#defconstruct}.
	 * @param ctx the parse tree
	 */
	void enterDefconstruct(MxstarParser.DefconstructContext ctx);
	/**
	 * Exit a parse tree produced by {@link MxstarParser#defconstruct}.
	 * @param ctx the parse tree
	 */
	void exitDefconstruct(MxstarParser.DefconstructContext ctx);
	/**
	 * Enter a parse tree produced by {@link MxstarParser#block}.
	 * @param ctx the parse tree
	 */
	void enterBlock(MxstarParser.BlockContext ctx);
	/**
	 * Exit a parse tree produced by {@link MxstarParser#block}.
	 * @param ctx the parse tree
	 */
	void exitBlock(MxstarParser.BlockContext ctx);
	/**
	 * Enter a parse tree produced by {@link MxstarParser#params}.
	 * @param ctx the parse tree
	 */
	void enterParams(MxstarParser.ParamsContext ctx);
	/**
	 * Exit a parse tree produced by {@link MxstarParser#params}.
	 * @param ctx the parse tree
	 */
	void exitParams(MxstarParser.ParamsContext ctx);
	/**
	 * Enter a parse tree produced by {@link MxstarParser#param}.
	 * @param ctx the parse tree
	 */
	void enterParam(MxstarParser.ParamContext ctx);
	/**
	 * Exit a parse tree produced by {@link MxstarParser#param}.
	 * @param ctx the parse tree
	 */
	void exitParam(MxstarParser.ParamContext ctx);
	/**
	 * Enter a parse tree produced by {@link MxstarParser#type}.
	 * @param ctx the parse tree
	 */
	void enterType(MxstarParser.TypeContext ctx);
	/**
	 * Exit a parse tree produced by {@link MxstarParser#type}.
	 * @param ctx the parse tree
	 */
	void exitType(MxstarParser.TypeContext ctx);
	/**
	 * Enter a parse tree produced by {@link MxstarParser#typeref_base}.
	 * @param ctx the parse tree
	 */
	void enterTyperef_base(MxstarParser.Typeref_baseContext ctx);
	/**
	 * Exit a parse tree produced by {@link MxstarParser#typeref_base}.
	 * @param ctx the parse tree
	 */
	void exitTyperef_base(MxstarParser.Typeref_baseContext ctx);
	/**
	 * Enter a parse tree produced by {@link MxstarParser#empty}.
	 * @param ctx the parse tree
	 */
	void enterEmpty(MxstarParser.EmptyContext ctx);
	/**
	 * Exit a parse tree produced by {@link MxstarParser#empty}.
	 * @param ctx the parse tree
	 */
	void exitEmpty(MxstarParser.EmptyContext ctx);
	/**
	 * Enter a parse tree produced by {@link MxstarParser#prim_type}.
	 * @param ctx the parse tree
	 */
	void enterPrim_type(MxstarParser.Prim_typeContext ctx);
	/**
	 * Exit a parse tree produced by {@link MxstarParser#prim_type}.
	 * @param ctx the parse tree
	 */
	void exitPrim_type(MxstarParser.Prim_typeContext ctx);
	/**
	 * Enter a parse tree produced by {@link MxstarParser#class_type}.
	 * @param ctx the parse tree
	 */
	void enterClass_type(MxstarParser.Class_typeContext ctx);
	/**
	 * Exit a parse tree produced by {@link MxstarParser#class_type}.
	 * @param ctx the parse tree
	 */
	void exitClass_type(MxstarParser.Class_typeContext ctx);
	/**
	 * Enter a parse tree produced by {@link MxstarParser#stmts}.
	 * @param ctx the parse tree
	 */
	void enterStmts(MxstarParser.StmtsContext ctx);
	/**
	 * Exit a parse tree produced by {@link MxstarParser#stmts}.
	 * @param ctx the parse tree
	 */
	void exitStmts(MxstarParser.StmtsContext ctx);
	/**
	 * Enter a parse tree produced by {@link MxstarParser#stmt}.
	 * @param ctx the parse tree
	 */
	void enterStmt(MxstarParser.StmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link MxstarParser#stmt}.
	 * @param ctx the parse tree
	 */
	void exitStmt(MxstarParser.StmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link MxstarParser#expr_stmt}.
	 * @param ctx the parse tree
	 */
	void enterExpr_stmt(MxstarParser.Expr_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link MxstarParser#expr_stmt}.
	 * @param ctx the parse tree
	 */
	void exitExpr_stmt(MxstarParser.Expr_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link MxstarParser#block_stmt}.
	 * @param ctx the parse tree
	 */
	void enterBlock_stmt(MxstarParser.Block_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link MxstarParser#block_stmt}.
	 * @param ctx the parse tree
	 */
	void exitBlock_stmt(MxstarParser.Block_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link MxstarParser#if_stmt}.
	 * @param ctx the parse tree
	 */
	void enterIf_stmt(MxstarParser.If_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link MxstarParser#if_stmt}.
	 * @param ctx the parse tree
	 */
	void exitIf_stmt(MxstarParser.If_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link MxstarParser#while_stmt}.
	 * @param ctx the parse tree
	 */
	void enterWhile_stmt(MxstarParser.While_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link MxstarParser#while_stmt}.
	 * @param ctx the parse tree
	 */
	void exitWhile_stmt(MxstarParser.While_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link MxstarParser#for_stmt}.
	 * @param ctx the parse tree
	 */
	void enterFor_stmt(MxstarParser.For_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link MxstarParser#for_stmt}.
	 * @param ctx the parse tree
	 */
	void exitFor_stmt(MxstarParser.For_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link MxstarParser#break_stmt}.
	 * @param ctx the parse tree
	 */
	void enterBreak_stmt(MxstarParser.Break_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link MxstarParser#break_stmt}.
	 * @param ctx the parse tree
	 */
	void exitBreak_stmt(MxstarParser.Break_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link MxstarParser#continue_stmt}.
	 * @param ctx the parse tree
	 */
	void enterContinue_stmt(MxstarParser.Continue_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link MxstarParser#continue_stmt}.
	 * @param ctx the parse tree
	 */
	void exitContinue_stmt(MxstarParser.Continue_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link MxstarParser#return_stmt}.
	 * @param ctx the parse tree
	 */
	void enterReturn_stmt(MxstarParser.Return_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link MxstarParser#return_stmt}.
	 * @param ctx the parse tree
	 */
	void exitReturn_stmt(MxstarParser.Return_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link MxstarParser#defvar_stmt}.
	 * @param ctx the parse tree
	 */
	void enterDefvar_stmt(MxstarParser.Defvar_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link MxstarParser#defvar_stmt}.
	 * @param ctx the parse tree
	 */
	void exitDefvar_stmt(MxstarParser.Defvar_stmtContext ctx);
	/**
	 * Enter a parse tree produced by the {@code newExpr}
	 * labeled alternative in {@link MxstarParser#expr}.
	 * @param ctx the parse tree
	 */
	void enterNewExpr(MxstarParser.NewExprContext ctx);
	/**
	 * Exit a parse tree produced by the {@code newExpr}
	 * labeled alternative in {@link MxstarParser#expr}.
	 * @param ctx the parse tree
	 */
	void exitNewExpr(MxstarParser.NewExprContext ctx);
	/**
	 * Enter a parse tree produced by the {@code unaryExpr}
	 * labeled alternative in {@link MxstarParser#expr}.
	 * @param ctx the parse tree
	 */
	void enterUnaryExpr(MxstarParser.UnaryExprContext ctx);
	/**
	 * Exit a parse tree produced by the {@code unaryExpr}
	 * labeled alternative in {@link MxstarParser#expr}.
	 * @param ctx the parse tree
	 */
	void exitUnaryExpr(MxstarParser.UnaryExprContext ctx);
	/**
	 * Enter a parse tree produced by the {@code primaryExpr}
	 * labeled alternative in {@link MxstarParser#expr}.
	 * @param ctx the parse tree
	 */
	void enterPrimaryExpr(MxstarParser.PrimaryExprContext ctx);
	/**
	 * Exit a parse tree produced by the {@code primaryExpr}
	 * labeled alternative in {@link MxstarParser#expr}.
	 * @param ctx the parse tree
	 */
	void exitPrimaryExpr(MxstarParser.PrimaryExprContext ctx);
	/**
	 * Enter a parse tree produced by the {@code memberExpr}
	 * labeled alternative in {@link MxstarParser#expr}.
	 * @param ctx the parse tree
	 */
	void enterMemberExpr(MxstarParser.MemberExprContext ctx);
	/**
	 * Exit a parse tree produced by the {@code memberExpr}
	 * labeled alternative in {@link MxstarParser#expr}.
	 * @param ctx the parse tree
	 */
	void exitMemberExpr(MxstarParser.MemberExprContext ctx);
	/**
	 * Enter a parse tree produced by the {@code binaryExpr}
	 * labeled alternative in {@link MxstarParser#expr}.
	 * @param ctx the parse tree
	 */
	void enterBinaryExpr(MxstarParser.BinaryExprContext ctx);
	/**
	 * Exit a parse tree produced by the {@code binaryExpr}
	 * labeled alternative in {@link MxstarParser#expr}.
	 * @param ctx the parse tree
	 */
	void exitBinaryExpr(MxstarParser.BinaryExprContext ctx);
	/**
	 * Enter a parse tree produced by the {@code arrayExpre}
	 * labeled alternative in {@link MxstarParser#expr}.
	 * @param ctx the parse tree
	 */
	void enterArrayExpre(MxstarParser.ArrayExpreContext ctx);
	/**
	 * Exit a parse tree produced by the {@code arrayExpre}
	 * labeled alternative in {@link MxstarParser#expr}.
	 * @param ctx the parse tree
	 */
	void exitArrayExpre(MxstarParser.ArrayExpreContext ctx);
	/**
	 * Enter a parse tree produced by the {@code funcCallExpr}
	 * labeled alternative in {@link MxstarParser#expr}.
	 * @param ctx the parse tree
	 */
	void enterFuncCallExpr(MxstarParser.FuncCallExprContext ctx);
	/**
	 * Exit a parse tree produced by the {@code funcCallExpr}
	 * labeled alternative in {@link MxstarParser#expr}.
	 * @param ctx the parse tree
	 */
	void exitFuncCallExpr(MxstarParser.FuncCallExprContext ctx);
	/**
	 * Enter a parse tree produced by the {@code assignExpr}
	 * labeled alternative in {@link MxstarParser#expr}.
	 * @param ctx the parse tree
	 */
	void enterAssignExpr(MxstarParser.AssignExprContext ctx);
	/**
	 * Exit a parse tree produced by the {@code assignExpr}
	 * labeled alternative in {@link MxstarParser#expr}.
	 * @param ctx the parse tree
	 */
	void exitAssignExpr(MxstarParser.AssignExprContext ctx);
	/**
	 * Enter a parse tree produced by {@link MxstarParser#creator}.
	 * @param ctx the parse tree
	 */
	void enterCreator(MxstarParser.CreatorContext ctx);
	/**
	 * Exit a parse tree produced by {@link MxstarParser#creator}.
	 * @param ctx the parse tree
	 */
	void exitCreator(MxstarParser.CreatorContext ctx);
	/**
	 * Enter a parse tree produced by {@link MxstarParser#functionCall}.
	 * @param ctx the parse tree
	 */
	void enterFunctionCall(MxstarParser.FunctionCallContext ctx);
	/**
	 * Exit a parse tree produced by {@link MxstarParser#functionCall}.
	 * @param ctx the parse tree
	 */
	void exitFunctionCall(MxstarParser.FunctionCallContext ctx);
}