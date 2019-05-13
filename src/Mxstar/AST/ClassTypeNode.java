package Mxstar.AST;

public class ClassTypeNode extends TypeNode{
    public String name = null;

    public ClassTypeNode(){ }
    public ClassTypeNode(String name){
        this.name = name;
    }
    @Override public void accept(ASTVisitor visitor) { visitor.visit(this); }

}
