package Mxstar.AST;

import Mxstar.ST.VariableSymbol;
import org.antlr.v4.runtime.Token;

public class IdexprNode extends ExprNode {
	public String name;

    public VariableSymbol symbol;

    public IdexprNode(Token token) {
        if(token != null) {
            this.name = token.getText();
            this.loc = new TokenLoc(token);
        }
    }

    @Override public void accept(ASTVisitor visitor) { visitor.visit(this); }
}