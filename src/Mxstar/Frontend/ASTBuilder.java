package Mxstar.Frontend;

import Mxstar.AST.*;
import Mxstar.Exceptions.*;
import Mxstar.Parser.MxstarBaseVisitor;
import Mxstar.Parser.MxstarParser.*;

import java.util.LinkedList;
import java.util.List;
import java.io.*;

import static Mxstar.Parser.MxstarParser.*;

public class ASTBuilder extends MxstarBaseVisitor<Object> {
	public ASTProgram astProg;
	public ErrorRecorder errorRecorder;

	public ASTBuilder(ErrorRecorder errorRecorder) {
		this.astProg = new ASTProgram();
		this.astProg.loc = new TokenLoc(0,0);
		this.errorRecorder = errorRecorder;
	}

	@Override public Object visitCompilation_unit(Compilation_unitContext ctx) {
		for (Top_defsContext c : ctx.top_defs()) {
			if (c.defclass() != null) {
				astProg.add(visitDefclass(c.defclass()));
			}
			else if (c.defun() != null) {
				astProg.add(visitDefun(c.defun()));
			}
			else {
				astProg.addAll(visitDefvars(c.defvars()));
			}
		}
		return null;
	}

	@Override public DefunNode visitDefun(DefunContext ctx) {
		DefunNode defun = new DefunNode();
		defun.retType = visitType(ctx.type());
		defun.name = ctx.NAME().getSymbol().getText();
		defun.paramList = visitParams(ctx.params());
		defun.funcBody = visitStmts(ctx.block().stmts());
		defun.loc = defun.retType.loc;
		return defun;
	}

	@Override public DefclassNode visitDefclass(DefclassContext ctx) {
		DefclassNode defclass = new DefclassNode();
		defclass.members = new LinkedList<>();
		defclass.methods = new LinkedList<>();
		defclass.loc = new TokenLoc(ctx);
		defclass.name = ctx.NAME().getSymbol().getText();

		for (DefvarsContext c : ctx.defvars()) {
			defclass.members.addAll(visitDefvars(c));
		}

		for (DefconstructContext c : ctx.defconstruct()) {
			if (defclass.constructor == null) defclass.constructor = visitDefconstruct(c);
			else errorRecorder.addRecord(new TokenLoc(c), "conflicting clas constructor");
		}

		for (DefunContext c : ctx.defun()) {
			DefunNode defun = visitDefun(c);
			if (defun.name.equals(defclass.name)) {
				errorRecorder.addRecord(new TokenLoc(c), "constructor can not have return value");
				continue;
			}
			defclass.methods.add(defun);
		}

		return defclass;
	}

	@Override public DefunNode visitDefconstruct(DefconstructContext ctx) {
		DefunNode defcon = new DefunNode();
		defcon.loc = new TokenLoc(ctx);
		defcon.name = ctx.NAME().getSymbol().getText();
		defcon.retType = new PrimTypeNode("void");
		defcon.paramList = visitParams(ctx.params());
		defcon.funcBody = visitStmts(ctx.block().stmts());
		return defcon;
	}

	@Override public List<DefvarNode> visitDefvars(DefvarsContext ctx) {
		TypeNode type = visitType(ctx.type());
		List<DefvarNode> defvars = visitVars(ctx.vars());
		for (DefvarNode c : defvars) {
			c.type = type;
		}
		return defvars;
	}

	@Override public List<DefvarNode> visitVars(VarsContext ctx) {
		List<DefvarNode> defvars = new LinkedList<>();
		for (VarContext c : ctx.var()) {
			defvars.add(visitVar(c));
		}
		return defvars;
	}

	@Override public DefvarNode visitVar(VarContext ctx) {
		DefvarNode defvar = new DefvarNode();
		defvar.loc = new TokenLoc(ctx);
		defvar.type = null;
		defvar.name = ctx.NAME().getSymbol().getText();
		if (ctx.expr() == null) defvar.init = null;
		else defvar.init = (ExprNode)ctx.expr().accept(this);
		return defvar;
	}

	@Override public TypeNode visitType(TypeContext ctx) {
		if (ctx.empty().isEmpty()) {
			TypeNode type = visitTyperef_base(ctx.typeref_base());
			return type;
		}
		else {
			ArrTypeNode type = new ArrTypeNode();
			type.type = visitTyperef_base(ctx.typeref_base());
			type.dim = ctx.empty().size();
			type.loc = new TokenLoc(ctx);
			return type;
		}
	}

	@Override public TypeNode visitTyperef_base(Typeref_baseContext ctx) {
		if (ctx.prim_type() != null) {
			return visitPrim_type(ctx.prim_type());
		}
		else {
			return visitClass_type(ctx.class_type());
		}
	}

	@Override public PrimTypeNode visitPrim_type(Prim_typeContext ctx) {
		PrimTypeNode pt = new PrimTypeNode();
		pt.name = ctx.token.getText();
		pt.loc = new TokenLoc(ctx);
		return pt;
	}

	@Override public ClassTypeNode visitClass_type(Class_typeContext ctx) {
		ClassTypeNode ctn = new ClassTypeNode();
		ctn.name = ctx.token.getText();
		ctn.loc = new TokenLoc(ctx);
		return ctn;
	}

	@Override public List<DefvarNode> visitParams(ParamsContext ctx) {
		List<DefvarNode> params = new LinkedList<>();
		if (ctx != null) {
			for (ParamContext c : ctx.param()) {
				params.add((DefvarNode) c.accept(this));
			}
		}
		return params;
	}

	@Override public DefvarNode visitParam(ParamContext ctx) {
		DefvarNode defvar = new DefvarNode();
		defvar.loc = new TokenLoc(ctx);
		defvar.type = (TypeNode)ctx.type().accept(this);
		defvar.name = ctx.NAME().getSymbol().getText();
		defvar.init = null;
		return defvar;
	}

	@Override public List<StmtNode> visitStmts(StmtsContext ctx) {
		List<StmtNode> stmts = new LinkedList<>();
		StmtNode tmp;
		if (ctx != null) {
			for (StmtContext c : ctx.stmt()) {
				tmp = visitStmt(c);
				if (tmp != null) stmts.add(tmp);
			}
		}
		return stmts;
	}

	@Override public StmtNode visitStmt(StmtContext ctx) {
		if (ctx != null) {
			if (ctx.expr_stmt() != null) {
				return visitExpr_stmt(ctx.expr_stmt());
			}
			else if (ctx.block_stmt() != null) {
				return visitBlock_stmt(ctx.block_stmt());
			}
			else if (ctx.if_stmt() != null) {
				return visitIf_stmt(ctx.if_stmt());
			}
			else if (ctx.while_stmt() != null) {
				return visitWhile_stmt(ctx.while_stmt());
			}
			else if (ctx.for_stmt() != null) {
				return visitFor_stmt(ctx.for_stmt());
			}
			else if (ctx.break_stmt() != null) {
				return visitBreak_stmt(ctx.break_stmt());
			}
			else if (ctx.continue_stmt() != null) {
				return visitContinue_stmt(ctx.continue_stmt());
			}
			else if (ctx.return_stmt() != null) {
				return visitReturn_stmt(ctx.return_stmt());
			}
			else if (ctx.defvar_stmt() != null) {
				return visitDefvar_stmt(ctx.defvar_stmt());
			}
			else return null;
		}
		else return null;
	}

	@Override public StmtNode visitExpr_stmt(Expr_stmtContext ctx) {
		ExprstmtNode ret = new ExprstmtNode();
		ret.loc = new TokenLoc(ctx);
		ret.expr = (ExprNode) ctx.expr().accept(this);
		return ret;
	}

	@Override public StmtNode visitBlock_stmt(Block_stmtContext ctx) {
		BlockstmtNode ret = new BlockstmtNode();
		ret.loc = new TokenLoc(ctx);
		ret.stmts = visitStmts(ctx.stmts());
		return ret;
	}

	@Override public StmtNode visitIf_stmt(If_stmtContext ctx) {
		IfstmtNode ret = new IfstmtNode();
		ret.loc = new TokenLoc(ctx);
		ret.condition = (ExprNode) ctx.expr().accept(this);
		ret.body = visitStmt(ctx.stmt(0));
		if (ctx.stmt(1) != null) ret.elseBody = visitStmt(ctx.stmt(1));
		else ret.elseBody = null;
		return ret;
	}

	@Override public StmtNode visitWhile_stmt(While_stmtContext ctx) {
		WhilestmtNode ret = new WhilestmtNode();
		ret.loc = new TokenLoc(ctx);
		ret.condition = (ExprNode) ctx.expr().accept(this);
		ret.body = visitStmt(ctx.stmt());
		return ret;
	}

	@Override public StmtNode visitFor_stmt(For_stmtContext ctx) {
		ForstmtNode ret = new ForstmtNode();
		ret.loc = new TokenLoc(ctx);
		if (ctx.forInit != null) ret.initCond = new ExprstmtNode((ExprNode)ctx.forInit.accept(this));
		else ret.initCond = null;
		if (ctx.forCondition != null) ret.endCond = (ExprNode)ctx.forCondition.accept(this);
		else ret.endCond = null;
		if (ctx.forUpdate != null) ret.upd = new ExprstmtNode((ExprNode)ctx.forUpdate.accept(this));
		else ret.upd = null;
		ret.body = visitStmt(ctx.stmt());
		return ret;
	}

	@Override public StmtNode visitBreak_stmt(Break_stmtContext ctx) {
		BreakstmtNode ret = new BreakstmtNode();
		ret.loc = new TokenLoc(ctx);
		return ret;
	}

	@Override public StmtNode visitContinue_stmt(Continue_stmtContext ctx) {
		ContinuestmtNode ret = new ContinuestmtNode();
		ret.loc = new TokenLoc(ctx);
		return ret;
	}

	@Override public StmtNode visitReturn_stmt(Return_stmtContext ctx) {
		ReturnstmtNode ret = new ReturnstmtNode();
		ret.loc = new TokenLoc(ctx);
		if (ctx.expr() != null) ret.retVal = (ExprNode) ctx.expr().accept(this);
		return ret;
	}

	@Override public StmtNode visitDefvar_stmt(Defvar_stmtContext ctx) {
		DefvarstmtNode ret = new DefvarstmtNode();
		ret.loc = new TokenLoc(ctx);
		ret.defVars = visitDefvars(ctx.defvars());
		return ret;
	}

	@Override public ExprNode visitPrimaryExpr(PrimaryExprContext ctx) {
		if (ctx.token == null) return (ExprNode)ctx.expr().accept(this);
		else if (ctx.token.getType() == NAME || ctx.token.getType() == THIS) return new IdexprNode(ctx.token);
		else return new LiterexprNode(ctx.token);
	}

	@Override public ExprNode visitBinaryExpr(BinaryExprContext ctx) {
		BinaryexprNode ret = new BinaryexprNode();
		ret.loc = new TokenLoc(ctx);
		ret.op = ctx.op.getText();
		ret.lhs = (ExprNode)ctx.lhs.accept(this);
		ret.rhs = (ExprNode)ctx.rhs.accept(this);
		return ret;
	}
	
	@Override public ExprNode visitArrayExpre(ArrayExpreContext ctx) {
		ArrexprNode ret = new ArrexprNode();
		ret.loc = new TokenLoc(ctx);
		ret.addr = (ExprNode)ctx.expr(0).accept(this);
		ret.offset = (ExprNode)ctx.expr(1).accept(this);
		return ret;
	}

	@Override public ExprNode visitNewExpr(NewExprContext ctx) {
		return visitCreator(ctx.creator());
	}

	@Override public ExprNode visitAssignExpr(AssignExprContext ctx) {
		AssignexprNode ret = new AssignexprNode();
		ret.loc = new TokenLoc(ctx);
		ret.lhs = (ExprNode)ctx.lhs.accept(this);
		ret.rhs = (ExprNode)ctx.rhs.accept(this);
		return ret;
	}
	
	@Override public ExprNode visitUnaryExpr(UnaryExprContext ctx) {
		UnaryexprNode ret = new UnaryexprNode();
		ret.loc = new TokenLoc(ctx);
		if(ctx.postfix != null) {
			if(ctx.postfix.getText().equals("++"))
				ret.op = "v++";
			else
				ret.op = "v--";
		} 
		else {
			switch(ctx.prefix.getText()) {
				case "++":
					ret.op = "++v";
					break;
				case "--":
					ret.op = "--v";
					break;
				default:
					ret.op = ctx.prefix.getText();
			}
		}
		ret.expr = (ExprNode)ctx.expr().accept(this);
		return ret;
	}
	
	@Override public ExprNode visitMemberExpr(MemberExprContext ctx) {
		MemberexprNode ret = new MemberexprNode();
		ret.loc = new TokenLoc(ctx);
		ret.obj = (ExprNode)ctx.expr().accept(this);
		if (ctx.NAME() != null) {
			ret.member = new IdexprNode(ctx.NAME().getSymbol());
			ret.method = null;
		}
		else {
			ret.method = visitFunctionCall(ctx.functionCall());
			ret.member = null;
		}
		return ret;
	}
	
	@Override public FuncallexprNode visitFuncCallExpr(FuncCallExprContext ctx) {
		return visitFunctionCall(ctx.functionCall());
	}

	@Override public FuncallexprNode visitFunctionCall(FunctionCallContext ctx) {
		FuncallexprNode ret = new FuncallexprNode();
		ret.loc = new TokenLoc(ctx);
		ret.name = ctx.NAME().getSymbol().getText();
		ret.args = new LinkedList<>();
		if (ctx.expr() != null) {
			for (ExprContext c : ctx.expr()) {
				ret.args.add((ExprNode) c.accept(this));
			}
		}
		return ret;
	}
	
	@Override public ExprNode visitCreator(CreatorContext ctx) {
		NewexprNode ret = new NewexprNode();
		ret.loc = new TokenLoc(ctx);
		ret.typeNode = visitTyperef_base(ctx.typeref_base());
		ret.defDims = new LinkedList<>();
		if (ctx.expr() != null) {
			for (ExprContext c : ctx.expr()) {
				ret.defDims.add((ExprNode)c.accept(this));
			}
		}
		if (ctx.empty() != null) ret.dims = ctx.empty().size();
		else ret.dims = 0;
		return ret;
	}

	public ASTProgram getAstProg() {
		return astProg;
	}
}