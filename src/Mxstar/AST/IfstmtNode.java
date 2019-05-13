package Mxstar.AST;

public class IfstmtNode extends StmtNode {
	public ExprNode condition;
	public StmtNode body;
	public StmtNode elseBody;

	@Override public void accept(ASTVisitor visitor) { visitor.visit(this); }
}