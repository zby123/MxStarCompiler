package Mxstar.AST;

import Mxstar.ST.FunctionSymbol;

import java.util.LinkedList;
import java.util.List;

public class DefunNode extends DefNode {
	public TypeNode retType = null;
	public String name = null;
	public List<DefvarNode> paramList;
	public List<StmtNode> funcBody;

	public FunctionSymbol symbol;
	@Override public void accept(ASTVisitor visitor) { visitor.visit(this); }
}
