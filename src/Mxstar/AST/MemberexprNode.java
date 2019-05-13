package Mxstar.AST;

public class MemberexprNode extends ExprNode {
    public ExprNode obj;
    public IdexprNode member;
    public FuncallexprNode method;

    @Override public void accept(ASTVisitor visitor) { visitor.visit(this); }

}
