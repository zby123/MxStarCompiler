package Mxstar.AST;

import java.util.List;

public class NewexprNode extends ExprNode {
    public TypeNode typeNode;
    public List<ExprNode> defDims;
    public int dims;

    @Override public void accept(ASTVisitor visitor) { visitor.visit(this); }

}
