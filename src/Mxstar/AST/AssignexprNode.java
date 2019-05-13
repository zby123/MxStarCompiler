package Mxstar.AST;

public class AssignexprNode extends ExprNode {
    public ExprNode lhs;
    public ExprNode rhs;

    @Override public void accept(ASTVisitor visitor) { visitor.visit(this); }
}
