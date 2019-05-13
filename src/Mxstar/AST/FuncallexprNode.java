package Mxstar.AST;

import Mxstar.ST.FunctionSymbol;

import java.util.List;

public class FuncallexprNode extends ExprNode {
    public String name;
    public List<ExprNode> args;

    public FunctionSymbol functionSymbol;

    @Override public void accept(ASTVisitor visitor) { visitor.visit(this); }
}
