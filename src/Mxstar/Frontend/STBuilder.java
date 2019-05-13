package Mxstar.Frontend;

import Mxstar.AST.*;
import Mxstar.ST.*;
import Mxstar.Exceptions.ErrorRecorder;

import java.util.Arrays;
import java.util.HashMap;
import java.io.*;

public class STBuilder implements ASTVisitor {
	public ErrorRecorder errRecorder;
	public GlobalScopeTree globalST;
	public ScopeTree curST;
	public FunctionSymbol curFunc;
	public String name;
	public HashMap<ScopeTree, ClassSymbol> classST;

	public STBuilder(ErrorRecorder errRec) {
		this.errRecorder = errRec;
		this.globalST = new GlobalScopeTree();
		this.curST = globalST;
		this.classST = new HashMap<>();
	}

	private void setCur(ScopeTree ST) { curST = ST; }
	private void recCur() { curST = curST.parent; assert(curST!=null); }

	private VariableType getVarType(TypeNode node) {
		if (node instanceof PrimTypeNode) {
			PrimitiveSymbol ps = globalST.getPrimitiveSymbol(((PrimTypeNode)node).name);
			if (ps != null) return new PrimitiveType(ps.name, ps);
			else return null;
		}
		else if (node instanceof ClassTypeNode) {
			ClassSymbol cs = globalST.getClassSymbol(((ClassTypeNode) node).name);
			if (cs != null) return new ClassType(cs.name, cs);
			else return null;
		}
		else if (node instanceof ArrTypeNode) {
			ArrTypeNode arr = (ArrTypeNode)node;
			VariableType vt;
			if (arr.dim == 1) {
				vt = getVarType(arr.type);
				if (arr.type instanceof PrimTypeNode && ((PrimTypeNode) arr.type).name.equals("void")) 
						errRecorder.addRecord(arr.loc, "can not create an array with type void");
			}
			else {
				ArrTypeNode nArr = new ArrTypeNode();
				nArr.type = arr.type;
				nArr.dim = arr.dim - 1;
				vt = getVarType(nArr);
			}
			if (vt != null) return new ArrayType(vt);
			else return null;
		}
		else {
			assert false;
			return null;
		}
	}

	private VariableSymbol getVarSymbol(String name, ScopeTree ST) {
		VariableSymbol vs = ST.getVariableSymbol(name);
		if (vs != null) { return vs; }
		else {
			if (ST.parent != null) return getVarSymbol(name, ST.parent);
			else return null;
		}
	}	

	private VariableSymbol getVarsSymbol(String name) {
		return getVarSymbol(name, curST);
	}

	private FunctionSymbol getFuncSymbol(String name, ScopeTree ST) {
		FunctionSymbol ret = ST.getFunctionSymbol(name);
		if (ret == null) {
			if (ST.parent != null) return getFuncSymbol(name, ST.parent);
			else return null;
		}
		return ret;
	}

	private FunctionSymbol getFuncSymbol(String name) {
		this.name = name;
		return getFuncSymbol(name, curST);
	}

	private void addClass(DefclassNode node) {
		if (globalST.getClassSymbol(node.name) != null) {
			errRecorder.addRecord(node.loc, "Class already defined");
			return;
		}
		if (globalST.getFunctionSymbol(node.name) != null) {
			errRecorder.addRecord(node.loc, "Class name conflict with function name");
			return;
		}
		ClassSymbol ret = new ClassSymbol();
		ret.name = node.name;
		ret.loc = node.loc;
		ret.classScopeTree = new ScopeTree(globalST);
		node.symbol = ret;
		classST.put(ret.classScopeTree, ret);
		globalST.putClassSymbol(ret.name, ret);
	}

	private void addClassFuncs(DefclassNode node) {
		ClassSymbol ret = globalST.getClassSymbol(node.name);
		setCur(ret.classScopeTree);
		if (node.constructor != null) addFunc(node.constructor, ret);
		for (DefunNode c : node.methods) 
			addFunc(c, ret);
		recCur();
	}
	private void defClassMembers(DefclassNode node) {
		ClassSymbol ret = globalST.getClassSymbol(node.name);
		setCur(ret.classScopeTree);
		for (DefvarNode c : node.members) {
			defVar(c);
		}
		recCur();
	}

	private void defClassFuncs(DefclassNode node) {
		ClassSymbol ret = globalST.getClassSymbol(node.name);
		setCur(ret.classScopeTree);
		if (node.constructor != null) defFunc(node.constructor, ret);
		for (DefunNode c : node.methods) 
			defFunc(c, ret);
		recCur();
	}

	private void addFunc(DefunNode node, ClassSymbol cs) {
		if (curST.getFunctionSymbol(node.name) != null) {
			errRecorder.addRecord(node.loc, "function already defined");
			return;
		}
		if (cs == null && globalST.getClassSymbol(node.name) != null) {
			errRecorder.addRecord(node.loc, "function name conflict with class name");
			return;
		}
		FunctionSymbol ret = new FunctionSymbol();
		if (cs == null) ret.name = node.name;
		else ret.name = cs.name + "." + node.name;
		ret.isGlobalFunction = (cs == null);
		ret.loc = node.loc;
		ret.returnType = getVarType(node.retType);
		if (ret.returnType == null) errRecorder.addRecord(node.retType.loc, "function must have return type");
		ret.functionScopeTree = null;
		if (cs != null) {
			ret.parameterNames.add("this");
			ret.parameterTypes.add(new ClassType(cs.name, cs));
		}
		for (DefvarNode c : node.paramList) {
			ret.parameterNames.add(c.name);
			VariableType type = getVarType(c.type);
			if (type == null) {
				errRecorder.addRecord(c.loc, "can not get param type");
			}
			ret.parameterTypes.add(type);
		}
		node.symbol = ret;
		curST.putFunctionSymbol(node.name, ret);
	}

	private void defVar(DefvarNode node) {
		VariableType type = getVarType(node.type);
		if (node.init != null) node.init.accept(this);
		if (type != null) {
			if (curST.getVariableSymbol(node.name) != null) {
				errRecorder.addRecord(node.loc, "variable already defined");
			}
			else {
				if (type instanceof PrimitiveType && ((PrimitiveType)type).name.equals("void")) {
					errRecorder.addRecord(node.loc, "can not define var with null type");
				}
				if (type instanceof ClassType && ((ClassType)type).name.equals("null")) {
					errRecorder.addRecord(node.loc, "can not define var with null type");
				}
				boolean inClass = classST.containsKey(curST);
				boolean inGlobal = curST == globalST;
				node.symbol = new VariableSymbol(node.name, type, node.loc, inClass, inGlobal);
				curST.putVariableSymbol(node.name, node.symbol);
				if (inGlobal && node.init != null) globalST.GV.add(node.symbol);
			}
		}
		else {
			errRecorder.addRecord(node.loc, "can not get type");
		}
	}
	
	private void defFunc(DefunNode node, ClassSymbol cs) {
		FunctionSymbol ret = curST.getFunctionSymbol(node.name);
		curFunc = ret;

		ret.functionScopeTree = new ScopeTree(curST);
		setCur(ret.functionScopeTree);
		if (cs != null) defVar(new DefvarNode(new ClassTypeNode(cs.name), "this", null));
		for (DefvarNode c : node.paramList) defVar(c);
		for (StmtNode c : node.funcBody) c.accept(this);
		recCur();
		curFunc = null;
		ret.finish();
	}

	@Override public void visit(ASTProgram node) {
		for (DefclassNode d : node.classes) addClass(d);
		for (DefclassNode d : node.classes) addClassFuncs(d);
		for (DefunNode d : node.functions) addFunc(d, null);
		if (errRecorder.errorOccured()) return;
		for (DefclassNode d : node.classes) defClassMembers(d);
		for (DefNode d : node.top_defs) {
			if (d instanceof DefvarNode) defVar((DefvarNode)d);
			if (d instanceof DefclassNode) defClassFuncs((DefclassNode)d);
			if (d instanceof DefunNode) defFunc((DefunNode)d, null);
		}
	}

	@Override public void visit(DefunNode node) {}
	@Override public void visit(PrimTypeNode node) {}
	@Override public void visit(ArrTypeNode node) {}
	@Override public void visit(DefclassNode node) {}
	@Override public void visit(DefvarNode node) { defVar(node); }
	@Override public void visit(ClassTypeNode node) {}
	@Override public void visit(TypeNode node) { }
	@Override public void visit(DefNode node) { 
		assert false; 
	}

	@Override public void visit(StmtNode node) { 
		assert false; 
	}

	@Override public void visit(ForstmtNode node) {
		if (node.initCond != null) node.initCond.accept(this);
		if (node.endCond != null) node.endCond.accept(this);
		if (node.upd != null) node.upd.accept(this);
		ScopeTree st = new ScopeTree(curST);
		setCur(st);
		if (node.body != null)
			node.body.accept(this);
		recCur();
	}

	@Override public void visit(WhilestmtNode node) {
		node.condition.accept(this);
		ScopeTree st = new ScopeTree(curST);
		setCur(st);
		node.body.accept(this);
		recCur();
	}

	@Override public void visit(IfstmtNode node) {
		node.condition.accept(this);
		ScopeTree st = new ScopeTree(curST);
		setCur(st);
		node.body.accept(this);
		recCur();
		ScopeTree st2 = new ScopeTree(curST);
		setCur(st2);
		if (node.elseBody != null) node.elseBody.accept(this);
		recCur();
	}

	@Override public void visit(ContinuestmtNode node) {}
	@Override public void visit(BreakstmtNode node) {}
	@Override public void visit(ReturnstmtNode node) {
		if (node.retVal != null) node.retVal.accept(this);
	}

	@Override public void visit(BlockstmtNode node) {
		ScopeTree st = new ScopeTree(curST);
		setCur(st);
		for (StmtNode s : node.stmts) s.accept(this);
		recCur();
	}

	@Override public void visit(DefvarstmtNode node) {
		for (DefvarNode d : node.defVars) defVar(d);
	}

	@Override public void visit(ExprstmtNode node) {
		node.expr.accept(this);
	}

	@Override public void visit(ExprNode node) {
		assert false;
	}

	@Override public void visit(IdexprNode node) {
		VariableSymbol vs = getVarsSymbol(node.name);
		if (vs == null) {
			errRecorder.addRecord(node.loc, "can not get variable");
			node.type = null;
			return;
		}
		node.symbol = vs;
		node.type = vs.type;
		if (vs.isGlobalVariable) {
			if (curFunc == null) {
				globalST.getFunctionSymbol(node.name);
			}
			else {
				curFunc.GV.add(vs);
				curFunc.isImportantFunc = true;
			}
		}
	}

	@Override public void visit(LiterexprNode node) {
		switch(node.typeName) {
			case "int":
				node.type = new PrimitiveType("int", globalST.getPrimitiveSymbol("int"));
				break;
			case "null":
				node.type = new ClassType("null", globalST.getClassSymbol("null"));
				break;
			case "bool":
				node.type = new PrimitiveType("bool", globalST.getPrimitiveSymbol("bool"));
				break;
			case "string":
				node.type = new ClassType("string", globalST.getClassSymbol("string"));
				break;
			default:
				assert false;
		}
	}
	
	@Override public void visit(FuncallexprNode node) {
		FunctionSymbol fs = getFuncSymbol(node.name);
		if (fs == null) {
			errRecorder.addRecord(node.loc, "can not get function");
			return;
		}
		for (ExprNode e : node.args) {
			e.accept(this);
		}
		node.type = fs.returnType;
		node.functionSymbol = fs;
		if (curFunc != null) curFunc.calleeSet.add(fs);
	}

	@Override public void visit(ArrexprNode node) {
		node.addr.accept(this);
		node.offset.accept(this);
		if (node.addr.type instanceof ArrayType) {
			node.type = ((ArrayType)node.addr.type).baseType;
		}
		else {
			node.type = null;
			errRecorder.addRecord(node.loc, "can not use index exprssion on non-array variable");
		}
	}

	@Override public void visit(NewexprNode node) {
		for (ExprNode e : node.defDims) e.accept(this);
		int dims = node.defDims.size() + node.dims;
		node.type = getVarType(node.typeNode);
		if (node.type == null) {
			errRecorder.addRecord(node.typeNode.loc, "can not get type");
			node.type = null;
			return;
		}
		if (dims == 0 && node.typeNode instanceof PrimTypeNode && ((PrimTypeNode)node.typeNode).name.equals("void")) {
			errRecorder.addRecord(node.loc, "can not new a void type variable");
		}
		for (int i = 0; i < dims; i++) node.type = new ArrayType(node.type);
	}

	@Override public void visit(MemberexprNode node) {
		node.obj.accept(this);
		if (node.obj.type instanceof PrimitiveType) {
			errRecorder.addRecord(node.obj.loc, "can not use access member of a non-class variable");
			node.type = null;
			return;
		}
		if (node.obj.type instanceof ArrayType) {
			ArrayType arrayType = (ArrayType)node.obj.type;
			if(node.method == null || !node.method.name.equals("size")) {
				errRecorder.addRecord(node.method.loc, "can not use access member of a non-class variable");
				node.type = null;
			}
			else {
				node.type = new PrimitiveType("int", globalST.getPrimitiveSymbol("int"));
			}
		}
		else {
			ClassType ct = (ClassType) node.obj.type;
			if (ct == null) return;
			if (node.member != null) {
				node.member.symbol = getVarSymbol(node.member.name, ct.symbol.classScopeTree);
				if (node.member.symbol == null) {
					errRecorder.addRecord(node.member.loc, "incorrect member");
					return;
				}
				node.member.type = node.member.symbol.type;
				node.type = node.member.type;
			}
			else {
				try {
					node.method.functionSymbol = getFuncSymbol(node.method.name, ct.symbol.classScopeTree);
				}
				catch (Exception e) {
					e.getStackTrace();
				}
				if (node.method.functionSymbol == null) {
					errRecorder.addRecord(node.method.loc, "incorrect member");
					node.type = null;
					return;
				}
				node.method.type = node.method.functionSymbol.returnType;
				node.type = node.method.type;
				for (ExprNode e : node.method.args) e.accept(this);
			}
		}
	}

	@Override public void visit(UnaryexprNode node) {
		node.expr.accept(this);
		node.type = node.expr.type;
	}

	private boolean ROP(String op) {
		switch(op) {
            case "==": case "!=": case "<": case "<=": case ">": case ">=":
                return true;
            default:
                return false;
        }
	}

	@Override public void visit(BinaryexprNode node) {
		node.lhs.accept(this);
		node.rhs.accept(this);
		if (ROP(node.op)) node.type = new PrimitiveType("bool", globalST.getPrimitiveSymbol("bool"));
		else node.type = node.lhs.type;
	}

	@Override public void visit(AssignexprNode node) {
		node.lhs.accept(this);
		node.rhs.accept(this);
		node.type = new PrimitiveType("void", globalST.getPrimitiveSymbol("void"));
	}
}