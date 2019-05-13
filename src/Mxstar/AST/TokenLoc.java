package Mxstar.AST;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;

public class TokenLoc {
	public final int row;
	public final int column;

	public TokenLoc(int row, int column) {
		this.row = row;
		this.column = column;
	}
	public TokenLoc(Token token) {
		row = token.getLine();
		column = token.getCharPositionInLine();
	}
	public TokenLoc(ParserRuleContext ctx) {
		this(ctx.start);
	}
	public TokenLoc(ASTNode node) {
		this.row = node.loc.row;
		this.column = node.loc.column;
	}

	@Override public String toString() {
		return "(" + row + "," + column + ")";
	}
}
