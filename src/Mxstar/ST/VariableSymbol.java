package Mxstar.ST;

import Mxstar.AST.TokenLoc;
import Mxstar.IR.*;

public class VariableSymbol {
	public String name;
	public VariableType type;
	public TokenLoc loc;

	public boolean isClassField;
	public boolean isGlobalVariable;
    public VirtualRegister vr;

	public VariableSymbol(String name, VariableType type, TokenLoc location, boolean isClassField, boolean isGlobalVariable) {
		this.name = name;
		this.type = type;
		this.loc = location;
		this.isClassField = isClassField;
		this.isGlobalVariable = isGlobalVariable;
	}
}
