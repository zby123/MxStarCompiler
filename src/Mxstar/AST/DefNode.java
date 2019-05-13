package Mxstar.AST;

public class DefNode extends ASTNode {
	@Override public void accept(ASTVisitor visitor) { visitor.visit(this); }
}
