package Mxstar.AST;

public abstract class ASTNode {
	public TokenLoc loc = null;
    public abstract void accept(ASTVisitor visitor);
}
