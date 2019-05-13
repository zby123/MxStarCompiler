package Mxstar.AST;

import Mxstar.ST.VariableType;

public abstract class ExprNode extends ASTNode {
    public VariableType type;
    public boolean modifiable;
}
