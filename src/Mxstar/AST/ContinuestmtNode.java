package Mxstar.AST;

public class ContinuestmtNode extends StmtNode {
	@Override public void accept(ASTVisitor visitor) { visitor.visit(this); }
}