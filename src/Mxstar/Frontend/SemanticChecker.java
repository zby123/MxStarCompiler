package Mxstar.Frontend;

import Mxstar.AST.*;
import Mxstar.ST.*;
import Mxstar.Exceptions.ErrorRecorder;

import java.io.*;

public class SemanticChecker implements ASTVisitor {
	GlobalScopeTree globalST;
	ErrorRecorder errRec;
	FunctionSymbol curFunc;
	int loopCnt;

	public SemanticChecker(GlobalScopeTree gst, ErrorRecorder errRec) {
		this.globalST = gst;
		this.errRec = errRec;
		this.loopCnt = 0;
	}

	@Override public void visit(ASTProgram node) {
		for (DefvarNode n : node.vars) n.accept(this);
		for (DefunNode n : node.functions) n.accept(this);
		for (DefclassNode n : node.classes) n.accept(this);
		FunctionSymbol mainFunc = globalST.getFunctionSymbol("main");
		if (mainFunc == null) {
			errRec.addRecord(node.loc, "main function doesn't exists");
		}
		else {
			if (mainFunc.returnType instanceof PrimitiveType && ((PrimitiveType) mainFunc.returnType).name.equals("int")) {
				if (mainFunc.parameterTypes.size() > 0)
					errRec.addRecord(mainFunc.loc, "main function can not have parameters");
			}
			else {
				errRec.addRecord(mainFunc.loc, "main function must have return value int");
			}
		}
	}

	@Override public void visit(DefNode node) {
		assert false;
	}

	@Override public void visit(DefunNode node) {
		curFunc = node.symbol;
		for (StmtNode n : node.funcBody) n.accept(this);
	}

	@Override public void visit(DefclassNode node) {
		for (DefunNode n : node.methods) n.accept(this);
		if (node.constructor != null) {
			node.constructor.accept(this);
			if (!node.constructor.name.equals(node.name)) {
				errRec.addRecord(node.constructor.loc, "class must have construct same as class name");
			}
		}
	}

	@Override public void visit(DefvarNode node) {
		if(node.init != null) {
			if(!node.symbol.type.match(node.init.type)) {
				errRec.addRecord(node.init.loc, "type conflict with the defined variable");
			}
		}
	}

	@Override public void visit(TypeNode node) {}
	@Override public void visit(ArrTypeNode node) {}
	@Override public void visit(PrimTypeNode node) {}
	@Override public void visit(ClassTypeNode node) {}
	@Override public void visit(StmtNode node) {
		assert false;
	}
	
	private void checkBoolExpr(ExprNode node) {
		if ((node.type instanceof PrimitiveType) && (((PrimitiveType)node.type).name.equals("bool"))) return;
		else errRec.addRecord(node.loc, "conditions must have bool type");
	}

	@Override public void visit(ForstmtNode node) {
		if (node.initCond != null) node.initCond.accept(this);
		if (node.endCond != null) {
			node.endCond.accept(this);
			checkBoolExpr(node.endCond);
		}
		if (node.upd != null) node.upd.accept(this);
		loopCnt++;
		if (node.body != null)
			node.body.accept(this);
		loopCnt--;
	}

	@Override public void visit(WhilestmtNode node) {
		node.condition.accept(this);
		checkBoolExpr(node.condition);
		loopCnt++;
		node.body.accept(this);
		loopCnt--;
	}

	@Override public void visit(IfstmtNode node) {
		node.condition.accept(this);
		checkBoolExpr(node.condition);
		node.body.accept(this);
		if (node.elseBody != null) node.elseBody.accept(this);
	}

	@Override public void visit(ContinuestmtNode node) {
		if (loopCnt == 0) errRec.addRecord(node.loc, "there is no loop to continue");
	}

	@Override public void visit(BreakstmtNode node) {
		if (loopCnt == 0) errRec.addRecord(node.loc, "there is no loop to continue");
	}

	@Override public void visit(ReturnstmtNode node) {
		VariableType reqType = curFunc.returnType;
		PrimitiveType voidType = new PrimitiveType("void", globalST.getPrimitiveSymbol("void"));
		if (reqType.match(voidType) && node.retVal != null) errRec.addRecord(node.loc, "can not have return type");
		VariableType retType;
		if (node.retVal == null) retType = voidType;
		else {
			retType = node.retVal.type;
			node.retVal.accept(this);
		}
		if (!reqType.match(retType))
			errRec.addRecord(node.loc, "return type not match");
	}

	@Override public void visit(BlockstmtNode node) {
		for (StmtNode s : node.stmts) s.accept(this);
	}

	@Override public void visit(DefvarstmtNode node) {
		for(DefvarNode d : node.defVars) d.accept(this);
	}

	@Override public void visit(ExprstmtNode node) {
		node.expr.accept(this);
	}

	@Override public void visit(ExprNode node) {
		assert false;
	}

	@Override public void visit(IdexprNode node) {
		if (node.name.equals("this")) node.modifiable = false;
		else node.modifiable = true;
	}

	@Override public void visit(LiterexprNode node) {
		node.modifiable = false;
	}

	@Override public void visit(ArrexprNode node) {
		node.addr.accept(this);
		node.offset.accept(this);
		node.modifiable = true;
	}

	@Override public void visit(FuncallexprNode node) {
		int paramCnt = node.functionSymbol.parameterTypes.size();
		int inClass = (node.functionSymbol.parameterNames.size() > 0 && node.functionSymbol.parameterNames.get(0).equals("this") ? 1 : 0);
		if (node.args.size() + inClass != paramCnt) {
			errRec.addRecord(node.loc, "the number of parameters numbers incorrect");
		}
		else {
			for (int i = 0; i < node.args.size(); i++) {
				node.args.get(i).accept(this);
				if (!node.args.get(i).type.match(node.functionSymbol.parameterTypes.get(i + inClass))) 
					errRec.addRecord(node.args.get(i).loc, "type of parameters not match");
			}
		}
		node.modifiable = false;
	}

	@Override public void visit(NewexprNode node) {
		for (ExprNode e : node.defDims) e.accept(this);
		node.modifiable = true;
	}

	@Override public void visit(MemberexprNode node) {
		node.obj.accept(this);
		if (node.obj.type instanceof ArrayType) {
			node.modifiable = false;
		}
		else {
			if (node.method != null) {
				node.method.accept(this);
				node.modifiable = node.method.modifiable;
			}
			else {
				node.modifiable = true;
			}
		}
	}

	private boolean checkStringType(VariableType type) {
		return type instanceof ClassType && ((ClassType) type).name.equals("string");
	}

	private boolean checkIntType(VariableType type) {
		return type instanceof PrimitiveType && ((PrimitiveType) type).name.equals("int");
	}

	private boolean checkBoolType(VariableType type) {
		return type instanceof PrimitiveType && ((PrimitiveType) type).name.equals("bool");
	}

	@Override public void visit(UnaryexprNode node) {
		node.expr.accept(this);
		boolean modifiableError = false;
		boolean typeError = false;
		boolean isInt = checkIntType(node.expr.type);
		boolean isBool = checkBoolType(node.expr.type);
		switch(node.op) {
			case "v++": case "v--":
				if(!node.expr.modifiable)
					modifiableError = true;
				if(!isInt)
					typeError = true;
				node.modifiable = false;
				break;
			case "++v": case "--v":
				if(!node.expr.modifiable)
					modifiableError = true;
				if(!isInt)
					typeError = true;
				node.modifiable = true;
				break;
			case "!":
				if(!isBool)
					typeError = true;
				node.modifiable = false;
				break;
			case "~":
				if(!isInt)
					typeError = true;
				node.modifiable = false;
				break;
			case "-":
				if(!isInt)
					typeError = true;
				node.modifiable = false;
				break;
			default:
				assert false;
		}
		if(typeError) {
			errRec.addRecord(node.loc, "the type can not do this unary process ");
		} else if(modifiableError) {
			errRec.addRecord(node.loc, "the expression is not modifiable");
		}
	}

	@Override public void visit(BinaryexprNode node) {
		node.lhs.accept(this);
		node.rhs.accept(this);
		if(!node.lhs.type.match(node.rhs.type)) {
			errRec.addRecord(node.loc, "type conflict of the binary operands");
		} else {
			boolean isInt = checkIntType(node.lhs.type);
			boolean isBool = checkBoolType(node.lhs.type);
			boolean isString = checkStringType(node.lhs.type);
			boolean typeError = false;
			switch(node.op) {
				case "*": case "/": case "%": case "-": case "<<": case ">>": case "&": case "|": case "^":
					if(!isInt)
						typeError = true;
					break;
				case "<=": case ">": case "<": case ">=": case "+":
					if(!isInt && !isString)
						typeError = true;
					break;
				case "&&": case "||":
					if(!isBool)
						typeError = true;
					break;
				case "==": case "!=":
					break;
				default:
					assert false;
			}
			if(typeError) {
				errRec.addRecord(node.loc, "the type can not do this operation");
			}
		}
		node.modifiable = false;
	}

	@Override public void visit(AssignexprNode node) {
		node.lhs.accept(this);
		node.rhs.accept(this);
		if(!node.lhs.type.match(node.rhs.type))
			errRec.addRecord(node.lhs.loc, "type conflict between lhs and rhs of the assign expression");
		if(!node.lhs.modifiable)
			errRec.addRecord(node.loc, "lhs of assign expression is not modifiable");
		node.modifiable = false;
	}
}