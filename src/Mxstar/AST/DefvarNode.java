package Mxstar.AST;

import Mxstar.ST.VariableSymbol;

public class DefvarNode extends DefNode {
    public TypeNode type = null;
    public String name = null;
    public ExprNode init = null;

    public VariableSymbol symbol;

    public DefvarNode() {}
    
    public DefvarNode(TypeNode type, String name, ExprNode init) {
        this.type = type;
        this.name = name;
        this.init = init;
        this.symbol = null;
    }
    @Override public void accept(ASTVisitor visitor) { visitor.visit(this); }
}
