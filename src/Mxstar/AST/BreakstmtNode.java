package Mxstar.AST;

public class BreakstmtNode extends StmtNode {
	@Override public void accept(ASTVisitor visitor) { visitor.visit(this); }
}