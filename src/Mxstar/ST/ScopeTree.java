package Mxstar.ST;

import org.antlr.v4.misc.OrderedHashMap;

import java.util.*;

public class ScopeTree {

	public Map<String,VariableSymbol> variables;
	public Map<String,FunctionSymbol> functions;
	public ScopeTree parent;
	public List<ScopeTree> children;
	public Map<String, Integer> offsets;
	private Integer currentOffset;


	public ScopeTree(ScopeTree parent) {
		this.variables = new LinkedHashMap<>();
		this.functions = new LinkedHashMap<>();
		this.parent = parent;
		this.children = new LinkedList<>();
		this.offsets = new OrderedHashMap<>();
		this.currentOffset = 0;
	}
	public VariableSymbol getVariableSymbol(String name) {
		return variables.get(name);
	}
	public void putVariableSymbol(String name, VariableSymbol variableSymbol) {
		variables.put(name, variableSymbol);
		offsets.put(name, currentOffset);
		currentOffset += 8;
	}
	public int getVariableOffset(String name) {
		return offsets.get(name);
	}
	public FunctionSymbol getFunctionSymbol(String name) { return functions.get(name); }
	public void putFunctionSymbol(String name, FunctionSymbol symbol) {
		functions.put(name, symbol);
	}
}
