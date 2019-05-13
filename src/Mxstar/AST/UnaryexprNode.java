package Mxstar.AST;

public class UnaryexprNode extends ExprNode {
    public String op;
    public ExprNode expr;

    @Override public void accept(ASTVisitor visitor) { visitor.visit(this); }
}