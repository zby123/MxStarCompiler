package Mxstar.ST;

import Mxstar.AST.TokenLoc;

public class PrimitiveSymbol extends TypeSymbol {
	public String name;
	public TokenLoc loc;

	public PrimitiveSymbol() { }
	public PrimitiveSymbol(String name) {
		this.name = name;
		this.loc = new TokenLoc(0, 0);
	}
}
