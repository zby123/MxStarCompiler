package Mxstar.AST;

public class ArrexprNode extends ExprNode {
    public ExprNode addr;
    public ExprNode offset;

    @Override public void accept(ASTVisitor visitor) { visitor.visit(this); }
}
