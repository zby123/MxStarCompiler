package Mxstar.AST;

import Mxstar.ST.ClassSymbol;

import java.util.List;

public class DefclassNode extends DefNode {
    public String name = null;
    public List<DefvarNode> members;
    public List<DefunNode> methods;
    public DefunNode constructor;

    public ClassSymbol symbol;
    @Override public void accept(ASTVisitor visitor) { visitor.visit(this); }
}
