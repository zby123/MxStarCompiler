package Mxstar.ST;

import Mxstar.AST.TokenLoc;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;

public class GlobalScopeTree extends ScopeTree {
	public Map<String,ClassSymbol> classes;
	public Map<String,PrimitiveSymbol> primitives;
	public HashSet<VariableSymbol> GV;

	public GlobalScopeTree() {
		super(null);
		classes = new LinkedHashMap<>();
		primitives = new LinkedHashMap<>();
		GV = new HashSet<>();
		addDefaultTypes();
	}

	public void putPrimitiveSymbol(String name, PrimitiveSymbol symbol) {
		primitives.put(name, symbol);
	}
	public PrimitiveSymbol getPrimitiveSymbol(String name) {
		return primitives.get(name);
	}
	public ClassSymbol getClassSymbol(String name) {
		return classes.get(name);
	}
	public void putClassSymbol(String name, ClassSymbol symbol) {
		classes.put(name, symbol);
	}

	private VariableType voidType() {
		return new PrimitiveType("void", primitives.get("void"));
	}
	private VariableType intType() {
		return new PrimitiveType("int", primitives.get("int"));
	}
	private VariableType stringType() {
		return new ClassType("string", classes.get("string"));
	}
	private FunctionSymbol stringLength() {
		FunctionSymbol f = new FunctionSymbol();
		f.name = "string.length";
		f.isGlobalFunction = true;
		f.isImportantFunc = false;
		f.loc = new TokenLoc(0, 0);
		f.parameterTypes.add(stringType());
		f.parameterNames.add("this");
		f.returnType = intType();
		return f;
	}
	private FunctionSymbol stringSubstring() {
		FunctionSymbol f = new FunctionSymbol();
		f.name = "string.substring";
		f.isGlobalFunction = true;
		f.isImportantFunc = false;
		f.returnType = stringType();
		f.loc = new TokenLoc(0, 0);
		f.parameterTypes.add(stringType());
		f.parameterNames.add("this");
		f.parameterTypes.add(intType());
		f.parameterNames.add("left");
		f.parameterTypes.add(intType());
		f.parameterNames.add("right");
		return f;
	}
	private FunctionSymbol stringParseInt() {
		FunctionSymbol f = new FunctionSymbol();
		f.name = "string.parseInt";
		f.isGlobalFunction = true;
		f.isImportantFunc = false;
		f.loc = new TokenLoc(0, 0);
		f.returnType = intType();
		f.parameterTypes.add(stringType());
		f.parameterNames.add("this");
		return f;
	}
	private FunctionSymbol stringOrd() {
		FunctionSymbol f = new FunctionSymbol();
		f.name = "string.ord";
		f.isGlobalFunction = true;
		f.isImportantFunc = false;
		f.loc = new TokenLoc(0, 0);
		f.returnType = intType();
		f.parameterTypes.add(stringType());
		f.parameterNames.add("this");
		f.parameterTypes.add(intType());
		f.parameterNames.add("pos");
		return f;
	}
	private FunctionSymbol lib_Print() {
		FunctionSymbol f = new FunctionSymbol();
		f.name = "print";
		f.isGlobalFunction = true;
		f.isImportantFunc = true;
		f.returnType = voidType();
		f.loc = new TokenLoc(0, 0);
		f.parameterTypes.add(stringType());
		f.parameterNames.add("str");
		return f;
	}
	private FunctionSymbol lib_Println() {
		FunctionSymbol f = new FunctionSymbol();
		f.name = "println";
		f.isGlobalFunction = true;
		f.isImportantFunc = true;
		f.returnType = voidType();
		f.loc = new TokenLoc(0 ,0);
		f.parameterTypes.add(stringType());
		f.parameterNames.add("str");
		return f;
	}
	private FunctionSymbol lib_GetString() {
		FunctionSymbol f = new FunctionSymbol();
		f.name = "getString";
		f.isImportantFunc = true;
		f.isGlobalFunction = true;
		f.returnType = stringType();
		f.loc = new TokenLoc(0, 0);
		return f;
	}
	private FunctionSymbol lib_GetInt() {
		FunctionSymbol f = new FunctionSymbol();
		f.name = "getInt";
		f.isImportantFunc = true;
		f.isGlobalFunction = true;
		f.returnType = intType();
		f.loc = new TokenLoc(0, 0);
		return f;
	}
	private FunctionSymbol lib_ToString() {
		FunctionSymbol f = new FunctionSymbol();
		f.name = "toString";
		f.isGlobalFunction = true;
		f.isImportantFunc = false;
		f.returnType = stringType();
		f.loc = new TokenLoc(0, 0);
		f.parameterTypes.add(intType());
		f.parameterNames.add("i");
		return f;
	}
	private void addDefaultPrimitives() {
		primitives.put("int", new PrimitiveSymbol("int"));
		primitives.put("void", new PrimitiveSymbol("void"));
		primitives.put("bool", new PrimitiveSymbol("bool"));
	}
	private void addDefaultNull() {
		ClassSymbol nullSymbol = new ClassSymbol();
		nullSymbol.name = "null";
		nullSymbol.loc = new TokenLoc(0,0);
		nullSymbol.classScopeTree = new ScopeTree(this);
		putClassSymbol("null", nullSymbol);
	}
	private void addDefaultString() {
		ClassSymbol stringClass = new ClassSymbol();
		putClassSymbol("string", stringClass);
		ScopeTree st = new ScopeTree(this);
		st.putFunctionSymbol("length", stringLength());
		st.putFunctionSymbol("substring", stringSubstring());
		st.putFunctionSymbol("parseInt", stringParseInt());
		st.putFunctionSymbol("ord", stringOrd());
		stringClass.name = "string";
		stringClass.loc = new TokenLoc(0, 0);
		stringClass.classScopeTree = st;
	}
	private void addDefaultFunctions() {
		putFunctionSymbol("print", lib_Print());
		putFunctionSymbol("println", lib_Println());
		putFunctionSymbol("getString", lib_GetString());
		putFunctionSymbol("getInt", lib_GetInt());
		putFunctionSymbol("toString", lib_ToString());
	}
	private void addDefaultTypes() {
		addDefaultPrimitives();
		addDefaultNull();
		addDefaultString();
		addDefaultFunctions();
	}
}
