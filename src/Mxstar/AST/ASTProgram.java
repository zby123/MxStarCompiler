package Mxstar.AST;

import java.util.*;

public class ASTProgram extends ASTNode {
	public List<DefunNode> functions;
	public List<DefclassNode> classes;
	public List<DefvarNode> vars;
	public List<DefNode> top_defs;

	public ASTProgram() {
		this.functions = new LinkedList<>();
		this.classes = new LinkedList<>();
		this.vars = new LinkedList<>();
		this.top_defs = new LinkedList<>();
	}
	
	public void add(DefunNode d) {
		functions.add(d);
		top_defs.add(d);
	}

	public void add(DefclassNode d) {
		classes.add(d);
		top_defs.add(d);
	}

	public void addAll(List<DefvarNode> d) {
		vars.addAll(d);
		top_defs.addAll(d);
	}

	public void accept(ASTVisitor visitor) { visitor.visit(this); }
}
