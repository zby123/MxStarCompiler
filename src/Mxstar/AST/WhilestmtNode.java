package Mxstar.AST;

public class WhilestmtNode extends StmtNode {
	public ExprNode condition;
	public StmtNode body;

	@Override public void accept(ASTVisitor visitor) { visitor.visit(this); }
}