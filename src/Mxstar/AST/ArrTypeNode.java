package Mxstar.AST;

public class ArrTypeNode extends TypeNode {
	public TypeNode type = null;
	public int dim = -1;

	@Override public void accept(ASTVisitor visitor) { visitor.visit(this); }
}
