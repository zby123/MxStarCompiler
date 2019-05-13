package Mxstar.AST;

import java.util.List;

public class DefvarstmtNode extends StmtNode {
	public List<DefvarNode> defVars;
	@Override public void accept(ASTVisitor visitor) { visitor.visit(this); }
}