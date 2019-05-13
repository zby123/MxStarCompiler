package Mxstar.Backend;

import Mxstar.AST.*;
import Mxstar.IR.*;
import Mxstar.ST.*;

import java.util.*;
import java.io.*;

import static Mxstar.IR.RegisterSet.*;

public class IRbuilder implements ASTVisitor {
	private GlobalScopeTree gst;

	private BasicBlock curBB;
	private Stack<BasicBlock> CondBB;
	private Stack<BasicBlock> endBB;
	private Function curFunc;
	private ClassSymbol curClass;
	private VirtualRegister curThis;

	private boolean inParamFlag;
	private boolean isInDefClass;
	private boolean inInlineFlag;

	public IRProgram irProgram;

	public IRbuilder(GlobalScopeTree gst) {
		this.gst = gst;
		this.irProgram = new IRProgram();
		this.endBB = new Stack<>();
		this.CondBB = new Stack<>();
		this.funcMap = new HashMap<>();
		this.defunMap = new HashMap<>();
		this.chooseMap = new HashMap<>();
		this.nonChooseMap = new HashMap<>();
		this.resMap = new HashMap<>();
		this.assignMap = new HashMap<>();
		this.inParamFlag = false;
		this.isInDefClass = false;
		this.inlineVR = new LinkedList<>();
		this.inlineAfter = new LinkedList<>();
		this.operationsCountMap = new HashMap<>();
		init();
	}

	@Override public void visit(ASTProgram node) {
		for (DefvarNode d : node.vars) {
			StaticData data = new StaticData(d.name, 8);
			VirtualRegister vr = new VirtualRegister(d.name);
			vr.spillPlace = new Memory(data);
			irProgram.staticData.add(data);
			d.symbol.vr = vr;
		}

		LinkedList<DefunNode> defn = new LinkedList<>();
		defn.addAll(node.functions);
		for (DefclassNode d : node.classes) {
			if (d.constructor != null) 
				defn.add(d.constructor);
			defn.addAll(d.methods);
		}
		for (DefunNode d : defn)
			defunMap.put(d.symbol.name, d);
		for (DefunNode d : defn) {
			if (funcMap.containsKey(d.symbol.name))
				continue;
			funcMap.put(d.symbol.name, new Function(Function.Type.UserDefined, d.symbol.name, !isVoidType(d.symbol.returnType)));
		}
		for (DefunNode d : node.functions)
			d.accept(this);
		for (DefclassNode d : node.classes) 
			d.accept(this);
		for (Function f : funcMap.values()) {
			if (f.type == Function.Type.UserDefined)
				f.finishBuild();
		}
		buildInitFunction(node);
	}

	@Override public void visit(DefNode node) {
		assert false;
	}

	@Override public void visit(DefclassNode node) {
		curClass = node.symbol;
		isInDefClass = true;
		if (node.constructor != null) node.constructor.accept(this);
		for (DefunNode d : node.methods)
			d.accept(this);
		isInDefClass = false;
	}

	@Override public void visit(DefunNode node) {
		curFunc = funcMap.get(node.symbol.name);
		curBB = curFunc.enterBB = new BasicBlock(curFunc, "enter");
		if (isInDefClass) {
			VirtualRegister vthis = new VirtualRegister("");
			curFunc.parameters.add(vthis);
			curThis = vthis;
		}
		inParamFlag = true;
		for (DefvarNode d : node.paramList) d.accept(this);
		inParamFlag = false;

		for (int i = 0; i < curFunc.parameters.size(); i++) {
			if (i < 6) {
				curBB.append(new MoveInst(curBB, curFunc.parameters.get(i), RegisterSet.vargs.get(i)));
			} else {
				curBB.append(new MoveInst(curBB, curFunc.parameters.get(i), curFunc.parameters.get(i).spillPlace));
			}
		}

		for (VariableSymbol vr : node.symbol.GV) {
			curBB.append(new MoveInst(curBB, vr.vr, vr.vr.spillPlace));
		}
		for (StmtNode s : node.funcBody) {
			s.accept(this);
		}
		if (!(curBB.tail instanceof ReturnInst)) {
			if (isVoidType(node.symbol.returnType)) {
				curBB.append(new ReturnInst(curBB));
			}
			else {
				curBB.append(new MoveInst(curBB, vrax, new Immediate(0)));
				curBB.append(new ReturnInst(curBB));
			}
		}
		LinkedList<ReturnInst> retInsts = new LinkedList<>();
		for (BasicBlock bb : curFunc.basicblocks) {
			for (IRInstruction inst = bb.head; inst != null; inst = inst.next) {
				if (inst instanceof ReturnInst) retInsts.add((ReturnInst) inst);
			}
		}

		BasicBlock leaveBB = new BasicBlock(curFunc, "leaveBB");
		for (ReturnInst retInst : retInsts) {
			retInst.prepend(new JumpInst(retInst.bb, leaveBB));
			retInst.remove();
		}
		leaveBB.append(new ReturnInst(leaveBB));
		curFunc.leaveBB = leaveBB;

		IRInstruction retInst = curFunc.leaveBB.tail;
		for (VariableSymbol vr : node.symbol.GV) {
			retInst.prepend(new MoveInst(retInst.bb, vr.vr.spillPlace, vr.vr));
		}
		funcMap.put(node.symbol.name, curFunc);
		irProgram.functions.add(curFunc);
	}

	private void boolAssign(ExprNode expr, Address vr) {
		BasicBlock trueBB = new BasicBlock(curFunc, "trueBB");
		BasicBlock falseBB = new BasicBlock(curFunc, "falseBB");
		BasicBlock mergeBB = new BasicBlock(curFunc, "merge");
		chooseMap.put(expr, trueBB);
		nonChooseMap.put(expr, falseBB);
		expr.accept(this);
		trueBB.append(new MoveInst(trueBB, vr, new Immediate(1)));
		falseBB.append(new MoveInst(falseBB, vr, new Immediate(0)));
		trueBB.append(new JumpInst(trueBB, mergeBB));
		falseBB.append(new JumpInst(falseBB, mergeBB));
		curBB = mergeBB;
	}

	private void assign(ExprNode expr, Address vr) {
		if(isBoolType(expr.type))
			boolAssign(expr, vr);
		else {
			assignMap.put(expr, vr);
			expr.accept(this);
			Operand result = resMap.get(expr);
			if(result != vr)
				curBB.append(new MoveInst(curBB, vr, result));
		}
	}

	@Override public void visit(DefvarNode node) {
		assert curFunc != null;
		VirtualRegister vr = new VirtualRegister(node.name);
		if (inInlineFlag) {
			inlineVR.getLast().put(node.symbol, vr);
			if (node.init != null) {
				assign(node.init, vr);
			}
		}
		else {
			if (inParamFlag) {
				if (curFunc.parameters.size() >= 6) vr.spillPlace = new StackSlot(vr.hint);
				curFunc.parameters.add(vr);
			}
			node.symbol.vr = vr;
			if (node.init != null) assign(node.init, vr);
		}
	}

	@Override public void visit(TypeNode node) {
		assert false;
	}

	@Override public void visit(ArrTypeNode node) {
		assert false;
	}

	@Override public void visit(PrimTypeNode node) {
		assert false;
	}

	@Override public void visit(ClassTypeNode node) {
		assert false;
	}

	@Override public void visit(StmtNode node) {
		assert false;
	}

	@Override public void visit(ForstmtNode node) {
		if (node.initCond != null) node.initCond.accept(this);
		BasicBlock bodyBB = new BasicBlock(curFunc, "forBodyBB");
		BasicBlock afterBB = new BasicBlock(curFunc, "forAfterBB");
		BasicBlock condBB = node.endCond == null ? bodyBB : new BasicBlock(curFunc, "forCondBB");
		BasicBlock updateBB = node.upd == null ? condBB : new BasicBlock(curFunc, "forUpdateBB");
		curBB.append(new JumpInst(curBB, condBB));
		CondBB.push(updateBB);
		endBB.push(afterBB);
		if (node.endCond != null) {
			chooseMap.put(node.endCond, bodyBB);
			nonChooseMap.put(node.endCond, afterBB);
			curBB = condBB;
			node.endCond.accept(this);
		}
		curBB = bodyBB;
		if (node.body != null)
			node.body.accept(this);
		curBB.append(new JumpInst(curBB, updateBB));
		if (node.upd != null) {
			curBB = updateBB;
			node.upd.accept(this);
			curBB.append(new JumpInst(curBB, condBB));
		}
		curBB = afterBB;
		endBB.pop();
		CondBB.pop();
	}

	@Override public void visit(WhilestmtNode node) {
		BasicBlock condBB = new BasicBlock(curFunc, "whileCondBB");
		BasicBlock bodyBB = new BasicBlock(curFunc, "whileBodyBB");
		BasicBlock afterBB = new BasicBlock(curFunc, "whileAfterBB");
		curBB.append(new JumpInst(curBB, condBB));
		CondBB.push(condBB);
		endBB.push(afterBB);
		curBB = condBB;
		chooseMap.put(node.condition, bodyBB);
		nonChooseMap.put(node.condition, afterBB);
		node.condition.accept(this);
		curBB = bodyBB;
		node.body.accept(this);
		curBB.append(new JumpInst(curBB, condBB));
		curBB = afterBB;
		CondBB.pop();
		endBB.pop();
	}

	@Override public void visit(IfstmtNode node) {
		BasicBlock thenBB = new BasicBlock(curFunc, "ifThenBB");
		BasicBlock afterBB = new BasicBlock(curFunc, "ifAfterBB");
		BasicBlock elseBB = node.elseBody == null ? afterBB :
				new BasicBlock(curFunc, "ifElseBB");
		chooseMap.put(node.condition, thenBB);
		nonChooseMap.put(node.condition, elseBB);
		node.condition.accept(this);
		curBB = thenBB;
		node.body.accept(this);
		curBB.append(new JumpInst(curBB, afterBB));
		if(node.elseBody != null) {
			curBB = elseBB;
			node.elseBody.accept(this);
			curBB.append(new JumpInst(curBB, afterBB));
		}
		curBB = afterBB;
	}

	@Override public void visit(ContinuestmtNode node) {
		curBB.append(new JumpInst(curBB, CondBB.peek()));
	}

	@Override public void visit(BreakstmtNode node) {
		curBB.append(new JumpInst(curBB, endBB.peek()));
	}

	@Override public void visit(ReturnstmtNode node) {
		if(node.retVal != null) {
			if(isBoolType(node.retVal.type)) {
				boolAssign(node.retVal, vrax);
			} else {
				node.retVal.accept(this);
				curBB.append(new MoveInst(curBB, vrax, resMap.get(node.retVal)));
			}
		}
		if(inInlineFlag) {
			curBB.append(new JumpInst(curBB, inlineAfter.getLast()));
		} else {
			curBB.append(new ReturnInst(curBB));
		}
	}

	@Override public void visit(BlockstmtNode node) {
		for(StmtNode statement : node.stmts)
			statement.accept(this);
	}

	@Override public void visit(DefvarstmtNode node) {
		for (DefvarNode d : node.defVars)
			d.accept(this);
	}

	@Override public void visit(ExprstmtNode node) {
		node.expr.accept(this);
	}

	private boolean isVoidType(VariableType type) {
		return type instanceof PrimitiveType && ((PrimitiveType) type).name.equals("void");
	}
	private boolean isIntType(VariableType type) {
		return type instanceof PrimitiveType && ((PrimitiveType) type).name.equals("int");
	}
	private boolean isBoolType(VariableType type) {
		return type instanceof PrimitiveType && ((PrimitiveType) type).name.equals("bool");
	}
	private boolean isStringType(VariableType type) {
		return type instanceof ClassType && ((ClassType) type).name.equals("string");
	}

	private void buildInitFunction(ASTProgram node) {
		irProgram.functions.add(lib_init);
		curFunc = lib_init;
		lib_init.GV = new HashSet<>(gst.GV);
		BasicBlock enterBB = new BasicBlock(curFunc, "enterBB");
		curBB = curFunc.enterBB = enterBB;
		for(DefvarNode vd : node.vars) {
			if(vd.init == null)
				continue;
			assign(vd.init, vd.symbol.vr);
		}
		curBB.append(new CallInst(curBB, vrax, funcMap.get("main")));
		curBB.append(new ReturnInst(curBB));
		curFunc.leaveBB = curBB;
		curFunc.finishBuild();
	}


	@Override public void visit(ExprNode node) {
		assert false;
	}

	private HashMap<String,Function> funcMap;
	private HashMap<String,DefunNode> defunMap;

	private HashMap<ExprNode,BasicBlock> chooseMap, nonChooseMap;
	private HashMap<ExprNode,Operand> resMap;
	private HashMap<ExprNode,Address> assignMap;

	@Override public void visit(IdexprNode node) {
		Operand operand;
		if(node.name.equals("this")) {
			operand = curThis;
		} else if(node.symbol.isClassField) {
			String fieldName = node.name;
			int offset = curClass.classScopeTree.getVariableOffset(fieldName);
			operand = new Memory(curThis, new Immediate(offset));
		} else {
			if(inInlineFlag) {
				operand = inlineVR.getLast().get(node.symbol);
			} else {
				operand = node.symbol.vr;
			}
			if(node.symbol.isGlobalVariable) {
				curFunc.GV.add(node.symbol);
			}
		}
		if (chooseMap.containsKey(node)) {
			curBB.append(new CJumpInst(curBB, operand, CJumpInst.CompareOp.NE,
					new Immediate(0), chooseMap.get(node), nonChooseMap.get(node)));
		} else {
			resMap.put(node, operand);
		}
	}

	@Override public void visit(LiterexprNode node) {
		Operand operand;
		switch(node.typeName) {
			case "int":
				operand = new Immediate(Integer.valueOf(node.value));
				break;
			case "null":
				operand = new Immediate(0);
				break;
			case "bool": {
				curBB.append(new JumpInst(curBB, node.value.equals("true") ? chooseMap.get(node) : nonChooseMap.get(node)));
				return;
			}
			default:
			{
				StaticData sd = new StaticData("static_string", node.value.substring(1, node.value.length()-1));
				irProgram.staticData.add(sd);
				operand = sd;
			}
		}
		resMap.put(node, operand);
	}

	@Override public void visit(ArrexprNode node) {
		node.addr.accept(this);
		Operand baseAddr = resMap.get(node.addr);
		node.offset.accept(this);
		Operand index = resMap.get(node.offset);

		VirtualRegister base;
		if(baseAddr instanceof Register) {
			base = (VirtualRegister) baseAddr;
		} else {
			base = new VirtualRegister("");
			curBB.append(new MoveInst(curBB, base, baseAddr));
		}
		Memory memory;

		if(index instanceof Immediate) {
			memory = new Memory(base, new Immediate(((Immediate) index).value * 8 + 8));
		} else if(index instanceof Register) {
			memory = new Memory(base, (Register) index, 8, new Immediate(8));
		} else if(index instanceof Memory){
			VirtualRegister vr = new VirtualRegister("");
			curBB.append(new MoveInst(curBB, vr, index));
			memory = new Memory(base, vr, 8, new Immediate(8));
		} else {
			assert false;
			memory = null;
		}
		if(chooseMap.containsKey(node))
			curBB.append(new CJumpInst(curBB, memory, CJumpInst.CompareOp.NE, new Immediate(0), chooseMap.get(node), nonChooseMap.get(node)));
		else
			resMap.put(node, memory);
	}

	@Override public void visit(FuncallexprNode node) {
		LinkedList<Operand> arguments = new LinkedList<>();
		if(!node.functionSymbol.isGlobalFunction)
			arguments.add(curThis);
		for(int i = 0; i < node.args.size(); i++) {
			ExprNode e = node.args.get(i);
			e.accept(this);
			arguments.add(resMap.get(e));
		}
		if(shouldInline(node.functionSymbol.name)) {
			workInline(node.functionSymbol.name, arguments);
		} else {
			curBB.append(new CallInst(curBB, vrax, funcMap.get(node.functionSymbol.name), arguments));
		}
		if(chooseMap.containsKey(node)) {
			curBB.append(new CJumpInst(curBB, vrax, CJumpInst.CompareOp.NE, new Immediate(0), chooseMap.get(node), nonChooseMap.get(node)));
		} else {
			if(!isVoidType(node.functionSymbol.returnType)) {
				VirtualRegister vr = new VirtualRegister("");
				curBB.append(new MoveInst(curBB, vr, vrax));
				resMap.put(node, vr);
			}
		}
	}

	private Operand allocateArray(LinkedList<Operand> dims, int baseBytes, Function constructor) {
		if(dims.size() == 0) {
			if(baseBytes == 0) {
				return new Immediate(0);
			} else {
				VirtualRegister retAddr = new VirtualRegister("");
				curBB.append(new CallInst(curBB, vrax, external_malloc, new Immediate(baseBytes)));
				curBB.append(new MoveInst(curBB, retAddr, vrax));
				if(constructor != null) {
					curBB.append(new CallInst(curBB, vrax, constructor, retAddr));
				} else {
					if(baseBytes == 8) {
						curBB.append(new MoveInst(curBB, new Memory(retAddr), new Immediate(0)));
					} else if(baseBytes == 8 * 2) {
						curBB.append(new BinaryInst(curBB, BinaryInst.BinaryOp.ADD, retAddr, new Immediate(8)));
						curBB.append(new MoveInst(curBB, new Memory(retAddr), new Immediate(0)));
						curBB.append(new BinaryInst(curBB, BinaryInst.BinaryOp.SUB, retAddr, new Immediate(8)));
					}
				}
				return retAddr;
			}
		} else {
			VirtualRegister addr = new VirtualRegister("");
			VirtualRegister size = new VirtualRegister("");
			VirtualRegister bytes = new VirtualRegister("");
			curBB.append(new MoveInst(curBB, size, dims.get(0)));
			curBB.append(new LeaInst(curBB, bytes, new Memory(size, 8, new Immediate(8))));
			curBB.append(new CallInst(curBB, vrax, external_malloc, bytes));
			curBB.append(new MoveInst(curBB, addr, vrax));
			curBB.append(new MoveInst(curBB, new Memory(addr), size));
			BasicBlock condBB = new BasicBlock(curFunc, "allocateCondBB");
			BasicBlock bodyBB = new BasicBlock(curFunc, "allocateBodyBB");
			BasicBlock afterBB = new BasicBlock(curFunc, "allocateAfterBB");
			curBB.append(new JumpInst(curBB, condBB));
			condBB.append(new CJumpInst(condBB, size, CJumpInst.CompareOp.G, new Immediate(0), bodyBB, afterBB));
			curBB = bodyBB;
			if(dims.size() == 1) {
				Operand pointer = allocateArray(new LinkedList<>(), baseBytes, constructor);
				curBB.append(new MoveInst(curBB, new Memory(addr, size, 8), pointer));
			} else {
				LinkedList<Operand> remainDims = new LinkedList<>();
				for(int i = 1; i < dims.size(); i++)
					remainDims.add(dims.get(i));
				Operand pointer = allocateArray(remainDims, baseBytes, constructor);
				curBB.append(new MoveInst(curBB, new Memory(addr, size, 8), pointer));
			}
			curBB.append(new UnaryInst(curBB, UnaryInst.UnaryOp.DEC, size));
			curBB.append(new JumpInst(curBB, condBB));
			curBB = afterBB;
			return addr;
		}
	}

	@Override public void visit(NewexprNode node) {
		Function constructor;
		if(node.dims == 0) {
			if(node.type instanceof ClassType) {
				ClassType classType = (ClassType) node.type;
				if(classType.name.equals("string"))
					constructor = null;
				else {
					FunctionSymbol fs = classType.symbol.classScopeTree.getFunctionSymbol(classType.name);
					if(fs == null)
						constructor = null;
					else
						constructor = funcMap.get(fs.name);
				}
			} else {
				constructor = null;
			}
		} else {
			constructor = null;
		}
		LinkedList<Operand> dims = new LinkedList<>();
		for(ExprNode expr : node.defDims) {
			expr.accept(this);
			dims.add(resMap.get(expr));
		}
		if(node.dims > 0 || node.typeNode instanceof PrimTypeNode ) {
			Operand pointer = allocateArray(dims, 0, null);
			resMap.put(node, pointer);
		} else {
			int bytes;
			if(node.type instanceof ClassType && ((ClassType) node.type).name.equals("string"))
				bytes = 8 * 2;
			else
				bytes = node.type.getBytes();
			Operand pointer = allocateArray(dims, bytes, constructor);
			resMap.put(node, pointer);
		}
	}

	private LinkedList<HashMap<VariableSymbol,VirtualRegister>> inlineVR;
	private LinkedList<BasicBlock> inlineAfter;
	private HashMap<FunctionSymbol,Integer> operationsCountMap;

	private int cOpLE(List<ExprNode> es) {
		int count = 0;
		for(ExprNode e : es)
			count += cOp(e);
		return count;
	}
	private int cOp(ExprNode e) {
		if(e == null) return 0;
		int count = 0;
		if (e instanceof ArrexprNode) {
			count += cOp(((ArrexprNode) e).addr);
			count += cOp(((ArrexprNode) e).offset);
		} else if (e instanceof FuncallexprNode) {
			count += cOpLE(((FuncallexprNode) e).args);
		} else if (e instanceof NewexprNode) {
			count += cOpLE(((NewexprNode) e).defDims);
		} else if (e instanceof UnaryexprNode) {
			count += cOp(((UnaryexprNode) e).expr);
			count += 1;
		} else if (e instanceof MemberexprNode) {
			if(((MemberexprNode) e).member != null)
				count += 1;
			else
				count += cOp(((MemberexprNode) e).method);
		} else if (e instanceof BinaryexprNode) {
			count += cOp(((BinaryexprNode) e).lhs);
			count += cOp(((BinaryexprNode) e).rhs);
		} else if (e instanceof AssignexprNode) {
			count += cOp(((AssignexprNode) e).lhs);
			count += cOp(((AssignexprNode) e).rhs);
		} else {
			count += 1;
		}
		return count;
	}

	private int cOp(StmtNode statement) {
		if(statement == null) return 0;
		int count = 0;
		if(statement instanceof IfstmtNode) {
			count += cOp(((IfstmtNode) statement).body);
			count += cOp(((IfstmtNode) statement).elseBody);
		} else if(statement instanceof WhilestmtNode) {
			count += cOp(((WhilestmtNode) statement).condition);
			count += cOp(((WhilestmtNode) statement).body);
		} else if(statement instanceof ForstmtNode) {
			count += cOp(((ForstmtNode) statement).initCond);
			count += cOp(((ForstmtNode) statement).endCond);
			count += cOp(((ForstmtNode) statement).upd);
			count += cOp(((ForstmtNode) statement).body);
		} else if(statement instanceof BlockstmtNode) {
			count += cOp(((BlockstmtNode) statement).stmts);
		} else if(statement instanceof ReturnstmtNode) {
			count += cOp(((ReturnstmtNode) statement).retVal);
		} else if(statement instanceof ExprstmtNode) {
			count += cOp(((ExprstmtNode) statement).expr);
		} else if(statement instanceof DefvarstmtNode) {
			for (DefvarNode d : ((DefvarstmtNode)statement).defVars)
				count += cOp(d.init);
		} else {
			count += 1;
		}
		return count;
	}
	private int cOp(List<StmtNode> statements) {
		int count = 0;
		for(StmtNode s : statements)
			count += cOp(s);
		return count;
	}

	private boolean shouldInline(String name) {
		if(!defunMap.containsKey(name)) 
			return false;
		DefunNode funcDeclaration = defunMap.get(name);
		if(!funcDeclaration.symbol.GV.isEmpty())
			return false;
		if(!funcDeclaration.symbol.isGlobalFunction)
			return false;
		List<StmtNode> body = funcDeclaration.funcBody;
		if(!operationsCountMap.containsKey(funcDeclaration.symbol))
			operationsCountMap.put(funcDeclaration.symbol, cOp(body));
		if(operationsCountMap.get(funcDeclaration.symbol) >= 20) return false;
		if(inlineVR.size() >= 4)
			return false;
		return true;
	}
	private void workInline(String name, LinkedList<Operand> arguments) {
		DefunNode funcDeclaration = defunMap.get(name);
		inlineVR.addLast(new HashMap<>());
		LinkedList<VirtualRegister> vrArguments = new LinkedList<>();
		for(Operand op : arguments) {
			VirtualRegister vr = new VirtualRegister("");
			curBB.append(new MoveInst(curBB, vr, op));
			vrArguments.add(vr);
		}
		for(int i = 0; i < funcDeclaration.paramList.size(); i++)
			inlineVR.getLast().put(funcDeclaration.paramList.get(i).symbol, vrArguments.get(i));
		BasicBlock inlineFuncBodyBB = new BasicBlock(curFunc, "inlineFuncBodyBB");
		BasicBlock inlineFuncAfterBB = new BasicBlock(curFunc, "inlineFuncAfterBB");
		inlineAfter.addLast(inlineFuncAfterBB);

		curBB.append(new JumpInst(curBB, inlineFuncBodyBB));
		curBB = inlineFuncBodyBB;
		VirtualRegister result = null;

		boolean oldIsInline = inInlineFlag;
		inInlineFlag = true;

		for(StmtNode st : funcDeclaration.funcBody)
			st.accept(this);

		if(!(curBB.tail instanceof JumpInst))
			curBB.append(new JumpInst(curBB, inlineFuncAfterBB));

		curBB = inlineFuncAfterBB;

		inlineAfter.removeLast();
		inlineVR.removeLast();
		inInlineFlag = oldIsInline;
	}

	@Override public void visit(MemberexprNode node) {
		VirtualRegister baseAddr = new VirtualRegister("");
		node.obj.accept(this);
		curBB.append(new MoveInst(curBB, baseAddr, resMap.get(node.obj)));

		if(node.obj.type instanceof ArrayType) {
			resMap.put(node, new Memory(baseAddr));
		} else if(node.obj.type instanceof ClassType) {
			ClassType classType = (ClassType) node.obj.type;
			Operand operand;
			if(node.member != null) {
				String fieldName = node.member.name;
				int offset = classType.symbol.classScopeTree.getVariableOffset(fieldName);
				operand = new Memory(baseAddr, new Immediate(offset));
			} else {
				Function function = funcMap.get(node.method.functionSymbol.name);
				LinkedList<Operand> arguments = new LinkedList<>();
				arguments.add(baseAddr);
				for(ExprNode e : node.method.args) {
					e.accept(this);
					Operand arg = resMap.get(e);
					arguments.add(arg);
				}
				if(shouldInline(node.method.functionSymbol.name)) {
					workInline(node.method.functionSymbol.name, arguments);
				} else {
					curBB.append(new CallInst(curBB, vrax, function, arguments));
				}
				if (!isVoidType(node.method.functionSymbol.returnType)) {
					VirtualRegister retValue = new VirtualRegister("");
					curBB.append(new MoveInst(curBB, retValue, vrax));
					operand = retValue;
				} else {
					operand = null;
				}
			}
			if(chooseMap.containsKey(node)) {
				curBB.append(new CJumpInst(curBB, operand, CJumpInst.CompareOp.NE, new Immediate(0), chooseMap.get(node), nonChooseMap.get(node)));
			} else {
				resMap.put(node, operand);
			}
		} else {
			assert false;
		}
	}

	@Override public void visit(UnaryexprNode node) {
		if(node.op.equals("!")) {
			chooseMap.put(node.expr, nonChooseMap.get(node));
			nonChooseMap.put(node.expr, chooseMap.get(node));
			node.expr.accept(this);
			return;
		}
		node.expr.accept(this);
		Operand operand = resMap.get(node.expr);
		switch(node.op) {
			case "v++": case "v--": {
				assert operand instanceof Address;
				VirtualRegister oldValue = new VirtualRegister("");
				curBB.append(new MoveInst(curBB, oldValue, operand));
				curBB.append(new UnaryInst(curBB, node.op.equals("v++") ? UnaryInst.UnaryOp.INC : UnaryInst.UnaryOp.DEC, (Address) operand));
				resMap.put(node, oldValue);
			}
			break;
			case "++v": case "--v": {
				assert operand instanceof Address;
				curBB.append(new UnaryInst(curBB, node.op.equals("++v") ? UnaryInst.UnaryOp.INC : UnaryInst.UnaryOp.DEC, (Address) operand));
				resMap.put(node, operand);
			}
			break;
			case "+": {
				resMap.put(node, operand);
			}
			break;
			case "-": case "~":{
				VirtualRegister vr = new VirtualRegister("");
				curBB.append(new MoveInst(curBB, vr, operand));
				curBB.append(new UnaryInst(curBB, node.op.equals("-") ? UnaryInst.UnaryOp.NEG : UnaryInst.UnaryOp.NOT, vr));
				resMap.put(node, vr);
			}
			break;
			default: assert false;
		}
	}

	private Operand doStringConcate(ExprNode lhs, ExprNode rhs) {
		Address result = new VirtualRegister("");
		lhs.accept(this);
		Operand olhs = resMap.get(lhs);
		rhs.accept(this);
		Operand orhs = resMap.get(rhs);
		VirtualRegister vr;
		if(olhs instanceof Memory && !(olhs instanceof StackSlot)) {
			vr = new VirtualRegister("");
			curBB.append(new MoveInst(curBB, vr, olhs));
			olhs = vr;
		}
		if(orhs instanceof Memory && !(orhs instanceof StackSlot)) {
			vr = new VirtualRegister("");
			curBB.append(new MoveInst(curBB, vr, orhs));
			orhs = vr;
		}
		curBB.append(new CallInst(curBB, vrax, lib_stringConcate, olhs, orhs));
		curBB.append(new MoveInst(curBB, result, vrax));
		return result;
	}

	private Operand doArithmeticBinary(String op, Address dest, ExprNode lhs, ExprNode rhs) {
		BinaryInst.BinaryOp bop = null;
		boolean isSpecial = false;
		boolean isRevertable = false;
		switch(op) {
			case "*": bop = BinaryInst.BinaryOp.MUL; isSpecial = true; break;
			case "/": bop = BinaryInst.BinaryOp.DIV; isSpecial = true; break;
			case "%": bop = BinaryInst.BinaryOp.MOD; isSpecial = true; break;
			case "+": bop = BinaryInst.BinaryOp.ADD; isRevertable = true; break;
			case "-": bop = BinaryInst.BinaryOp.SUB; break;
			case ">>": bop = BinaryInst.BinaryOp.SAR; break;
			case "<<": bop = BinaryInst.BinaryOp.SAL; break;
			case "&": bop = BinaryInst.BinaryOp.AND; isRevertable = true; break;
			case "|": bop = BinaryInst.BinaryOp.OR; isRevertable = true; break;
			case "^": bop = BinaryInst.BinaryOp.XOR; isRevertable = true; break;
		}
		lhs.accept(this);
		Operand olhs = resMap.get(lhs);
		rhs.accept(this);
		Operand orhs = resMap.get(rhs);
		Address result = new VirtualRegister("");

		if(!isSpecial) {
			if(olhs == dest) {
				result = dest;
				if(op.equals("<<") || op.equals(">>")) {
					curBB.append(new MoveInst(curBB, vrcx, orhs));
					curBB.append(new BinaryInst(curBB, bop, result, vrcx));
				} else {
					curBB.append(new BinaryInst(curBB, bop, result, orhs));
				}
			} else if(isRevertable && orhs == dest) {
				result = dest;
				curBB.append(new BinaryInst(curBB, bop, result, olhs));
			} else {
				if(op.equals("<<") || op.equals(">>")) {
					curBB.append(new MoveInst(curBB, result, olhs));
					curBB.append(new MoveInst(curBB, vrcx, orhs));
					curBB.append(new BinaryInst(curBB, bop, result, vrcx));
				} else {
					curBB.append(new MoveInst(curBB, result, olhs));
					curBB.append(new BinaryInst(curBB, bop, result, orhs));
				}
			}
		} else {
			if (op.equals("*")) {
				curBB.append(new MoveInst(curBB, vrax, olhs));
				curBB.append(new BinaryInst(curBB, bop, null, orhs));
				curBB.append(new MoveInst(curBB, result, vrax));
			} else {
				/*if (op.equals("%") && orhs instanceof Immediate && ((Immediate)orhs).value == 10000) {
					curBB.append(new CallInst(curBB, vrax, lib_mod, olhs));
					curBB.append(new MoveInst(curBB, result, vrax));
				}
				else {*/
					curBB.append(new MoveInst(curBB, vrax, olhs));
					curBB.append(new CdqInst(curBB));
					curBB.append(new BinaryInst(curBB, bop, null, orhs));
					if (op.equals("/")) {
						curBB.append(new MoveInst(curBB, result, vrax));
					} else {
						curBB.append(new MoveInst(curBB, result, vrdx));
					}
				//}
			}
		}
		return result;
	}
	private void doLogicalBinary(String op, ExprNode lhs, ExprNode rhs, BasicBlock trueBB, BasicBlock falseBB) {
		BasicBlock checkSecondBB = new BasicBlock(curFunc, "secondConditionBB");
		if(op.equals("&&")) {
			nonChooseMap.put(lhs, falseBB);
			chooseMap.put(lhs, checkSecondBB);
		} else {
			chooseMap.put(lhs, trueBB);
			nonChooseMap.put(lhs, checkSecondBB);
		}
		lhs.accept(this);
		curBB = checkSecondBB;
		chooseMap.put(rhs, trueBB);
		nonChooseMap.put(rhs, falseBB);
		rhs.accept(this);
	}
	private void doRelationBinary(BinaryexprNode fnode, String op, ExprNode lhs, ExprNode rhs, BasicBlock trueBB, BasicBlock falseBB) {
		lhs.accept(this);
		Operand olhs = resMap.get(lhs);
		rhs.accept(this);
		Operand orhs = resMap.get(rhs);

		CJumpInst.CompareOp cop = null;
		switch(op) {
			case ">": cop = CJumpInst.CompareOp.G; break;
			case "<": cop = CJumpInst.CompareOp.L; break;
			case ">=": cop = CJumpInst.CompareOp.GE; break;
			case "<=": cop = CJumpInst.CompareOp.LE; break;
			case "==": cop = CJumpInst.CompareOp.E; break;
			case "!=": cop = CJumpInst.CompareOp.NE; break;
		}
		if(lhs.type instanceof ClassType && ((ClassType) lhs.type).name.equals("string")) { //  str (<|<=|>|>=|==|!=) str
			VirtualRegister scr = new VirtualRegister("");
			curBB.append(new CallInst(curBB, vrax, lib_stringCompare, olhs, orhs));
			curBB.append(new MoveInst(curBB, scr, vrax));
			if (trueBB != null && falseBB != null)
				curBB.append(new CJumpInst(curBB, scr, cop, new Immediate(0), trueBB, falseBB));
			else 
				resMap.put(fnode, scr);
		} else {
			if(olhs instanceof Memory && orhs instanceof Memory) {
				VirtualRegister vr = new VirtualRegister("");
				curBB.append(new MoveInst(curBB, vr, olhs));
				olhs = vr;
			}
			if (trueBB != null && falseBB != null)
				curBB.append(new CJumpInst(curBB, olhs, cop, orhs, trueBB, falseBB));
		}
	}

	@Override public void visit(BinaryexprNode node) {
		switch(node.op) {
			case "*": case "/": case "%": case "+": case "-":
			case ">>": case "<<": case "&": case "|": case "^":
				if(node.op.equals("+") && isStringType(node.type)) {
					resMap.put(node, doStringConcate(node.lhs, node.rhs));
				} else {
					resMap.put(node, doArithmeticBinary(node.op, assignMap.get(node), node.lhs, node.rhs));
				}
				break;
			case "<": case ">": case "==": case ">=": case "<=": case "!=":
				doRelationBinary(node, node.op, node.lhs, node.rhs, chooseMap.get(node), nonChooseMap.get(node));
				break;
			case "&&": case "||":
				doLogicalBinary(node.op, node.lhs, node.rhs, chooseMap.get(node), nonChooseMap.get(node));
				break;
			default:
				assert false;
		}
	}

	@Override public void visit(AssignexprNode node) {
		node.lhs.accept(this);
		Operand lvalue = resMap.get(node.lhs);
		assert lvalue instanceof Address;
		assign(node.rhs, (Address)lvalue);
	}

	private static Function lib_print;
	private static Function lib_println;
	private static Function lib_getString;
	private static Function lib_getInt;
	private static Function lib_toString;
	private static Function lib_string_length;
	private static Function lib_string_substring;
	private static Function lib_string_parseInt;
	private static Function lib_string_ord;
	private static Function lib_hasValue;
	private static Function lib_getValue;
	private static Function lib_setValue;
	private static Function lib_stringConcate;
	private static Function lib_stringCompare;
	private static Function external_malloc;
	private static Function lib_init;
	private static Function lib_mod;

	private void init() {
		lib_print = new Function(Function.Type.Library, "print", false);
		funcMap.put("print", lib_print);
		lib_println = new Function(Function.Type.Library, "println", false);
		funcMap.put("println", lib_println);
		lib_getString = new Function(Function.Type.Library, "getString", false);
		funcMap.put("getString", lib_getString);
		lib_getInt = new Function(Function.Type.Library, "getInt", true);
		funcMap.put("getInt", lib_getInt);
		lib_toString = new Function(Function.Type.Library, "toString", true);
		funcMap.put("toString", lib_toString);
		lib_string_length = new Function(Function.Type.Library, "stringLength", true) ;
		funcMap.put("string.length", lib_string_length);
		lib_string_substring = new Function(Function.Type.Library, "stringSubstring", true);
		funcMap.put("string.substring", lib_string_substring);
		lib_string_parseInt = new Function(Function.Type.Library, "stringParseInt", true);
		funcMap.put("string.parseInt", lib_string_parseInt);
		lib_string_ord = new Function(Function.Type.Library, "stringOrd", true);
		funcMap.put("string.ord", lib_string_ord);
		lib_mod = new Function(Function.Type.Library, "mod10000", true);
		funcMap.put("mod10000", lib_mod);

		lib_stringConcate = new Function(Function.Type.Library, "stringConcat", true);
		lib_stringCompare = new Function(Function.Type.Library, "stringCmp", true);


		lib_init = new Function(Function.Type.Library, "init", true);

		external_malloc = new Function(Function.Type.External, "malloc", true);

	}
}