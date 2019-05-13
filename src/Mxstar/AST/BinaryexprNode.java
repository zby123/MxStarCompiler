package Mxstar.AST;

public class BinaryexprNode extends ExprNode {
    public String op;
    public ExprNode lhs;
    public ExprNode rhs;

    @Override public void accept(ASTVisitor visitor) { visitor.visit(this); }
}

