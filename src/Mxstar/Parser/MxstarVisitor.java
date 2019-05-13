// Generated from Mxstar.g4 by ANTLR 4.7.1
package Mxstar.Parser;
import org.antlr.v4.runtime.tree.ParseTreeVisitor;

/**
 * This interface defines a complete generic visitor for a parse tree produced
 * by {@link MxstarParser}.
 *
 * @param <T> The return type of the visit operation. Use {@link Void} for
 * operations with no return type.
 */
public interface MxstarVisitor<T> extends ParseTreeVisitor<T> {
	/**
	 * Visit a parse tree produced by {@link MxstarParser#compilation_unit}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCompilation_unit(MxstarParser.Compilation_unitContext ctx);
	/**
	 * Visit a parse tree produced by {@link MxstarParser#top_defs}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTop_defs(MxstarParser.Top_defsContext ctx);
	/**
	 * Visit a parse tree produced by {@link MxstarParser#defvars}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDefvars(MxstarParser.DefvarsContext ctx);
	/**
	 * Visit a parse tree produced by {@link MxstarParser#vars}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitVars(MxstarParser.VarsContext ctx);
	/**
	 * Visit a parse tree produced by {@link MxstarParser#var}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitVar(MxstarParser.VarContext ctx);
	/**
	 * Visit a parse tree produced by {@link MxstarParser#defun}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDefun(MxstarParser.DefunContext ctx);
	/**
	 * Visit a parse tree produced by {@link MxstarParser#defclass}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDefclass(MxstarParser.DefclassContext ctx);
	/**
	 * Visit a parse tree produced by {@link MxstarParser#defconstruct}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDefconstruct(MxstarParser.DefconstructContext ctx);
	/**
	 * Visit a parse tree produced by {@link MxstarParser#block}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBlock(MxstarParser.BlockContext ctx);
	/**
	 * Visit a parse tree produced by {@link MxstarParser#params}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitParams(MxstarParser.ParamsContext ctx);
	/**
	 * Visit a parse tree produced by {@link MxstarParser#param}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitParam(MxstarParser.ParamContext ctx);
	/**
	 * Visit a parse tree produced by {@link MxstarParser#type}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitType(MxstarParser.TypeContext ctx);
	/**
	 * Visit a parse tree produced by {@link MxstarParser#typeref_base}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTyperef_base(MxstarParser.Typeref_baseContext ctx);
	/**
	 * Visit a parse tree produced by {@link MxstarParser#empty}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitEmpty(MxstarParser.EmptyContext ctx);
	/**
	 * Visit a parse tree produced by {@link MxstarParser#prim_type}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPrim_type(MxstarParser.Prim_typeContext ctx);
	/**
	 * Visit a parse tree produced by {@link MxstarParser#class_type}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitClass_type(MxstarParser.Class_typeContext ctx);
	/**
	 * Visit a parse tree produced by {@link MxstarParser#stmts}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStmts(MxstarParser.StmtsContext ctx);
	/**
	 * Visit a parse tree produced by {@link MxstarParser#stmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStmt(MxstarParser.StmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link MxstarParser#expr_stmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExpr_stmt(MxstarParser.Expr_stmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link MxstarParser#block_stmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBlock_stmt(MxstarParser.Block_stmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link MxstarParser#if_stmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIf_stmt(MxstarParser.If_stmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link MxstarParser#while_stmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitWhile_stmt(MxstarParser.While_stmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link MxstarParser#for_stmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFor_stmt(MxstarParser.For_stmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link MxstarParser#break_stmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBreak_stmt(MxstarParser.Break_stmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link MxstarParser#continue_stmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitContinue_stmt(MxstarParser.Continue_stmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link MxstarParser#return_stmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitReturn_stmt(MxstarParser.Return_stmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link MxstarParser#defvar_stmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDefvar_stmt(MxstarParser.Defvar_stmtContext ctx);
	/**
	 * Visit a parse tree produced by the {@code newExpr}
	 * labeled alternative in {@link MxstarParser#expr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNewExpr(MxstarParser.NewExprContext ctx);
	/**
	 * Visit a parse tree produced by the {@code unaryExpr}
	 * labeled alternative in {@link MxstarParser#expr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitUnaryExpr(MxstarParser.UnaryExprContext ctx);
	/**
	 * Visit a parse tree produced by the {@code primaryExpr}
	 * labeled alternative in {@link MxstarParser#expr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPrimaryExpr(MxstarParser.PrimaryExprContext ctx);
	/**
	 * Visit a parse tree produced by the {@code memberExpr}
	 * labeled alternative in {@link MxstarParser#expr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMemberExpr(MxstarParser.MemberExprContext ctx);
	/**
	 * Visit a parse tree produced by the {@code binaryExpr}
	 * labeled alternative in {@link MxstarParser#expr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBinaryExpr(MxstarParser.BinaryExprContext ctx);
	/**
	 * Visit a parse tree produced by the {@code arrayExpre}
	 * labeled alternative in {@link MxstarParser#expr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitArrayExpre(MxstarParser.ArrayExpreContext ctx);
	/**
	 * Visit a parse tree produced by the {@code funcCallExpr}
	 * labeled alternative in {@link MxstarParser#expr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFuncCallExpr(MxstarParser.FuncCallExprContext ctx);
	/**
	 * Visit a parse tree produced by the {@code assignExpr}
	 * labeled alternative in {@link MxstarParser#expr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAssignExpr(MxstarParser.AssignExprContext ctx);
	/**
	 * Visit a parse tree produced by {@link MxstarParser#creator}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCreator(MxstarParser.CreatorContext ctx);
	/**
	 * Visit a parse tree produced by {@link MxstarParser#functionCall}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFunctionCall(MxstarParser.FunctionCallContext ctx);
}