package Mxstar.AST;

import java.util.List;


public class BlockstmtNode extends StmtNode {
	public List<StmtNode> stmts;

	@Override public void accept(ASTVisitor visitor) { visitor.visit(this); }
}