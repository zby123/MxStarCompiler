package Mxstar.AST;

public class PrimTypeNode extends TypeNode {
	public String name;
	public PrimTypeNode() {}
	public PrimTypeNode(String name) {
		this.name = name;
	}
	@Override public void accept(ASTVisitor visitor) { visitor.visit(this); }
}