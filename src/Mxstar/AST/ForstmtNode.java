package Mxstar.AST;

public class ForstmtNode extends StmtNode {
	public StmtNode initCond;
	public ExprNode endCond;
	public StmtNode upd;
	public StmtNode body;

	@Override public void accept(ASTVisitor visitor) { visitor.visit(this); }
}