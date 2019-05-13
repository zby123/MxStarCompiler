package Mxstar.AST;

public class ReturnstmtNode extends StmtNode {
	public ExprNode retVal;
	public ReturnstmtNode() {}
	public ReturnstmtNode(ExprNode retVal) {
		this.retVal = retVal;
	}
	@Override public void accept(ASTVisitor visitor) { visitor.visit(this); }
}