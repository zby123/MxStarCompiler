package Mxstar.AST;


public class ExprstmtNode extends StmtNode {
    public ExprNode expr = null;

    public ExprstmtNode() {
    }
    public ExprstmtNode(ExprNode expr) {
        this.expr = expr;
    }

    @Override public void accept(ASTVisitor visitor) { visitor.visit(this); }

}
