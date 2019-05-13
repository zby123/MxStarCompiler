package Mxstar.AST;

public interface ASTVisitor{
    void visit(ASTProgram node);
    void visit(DefclassNode node);
    void visit(DefunNode node);
    void visit(DefNode node);
    void visit(DefvarNode node);
    void visit(ExprNode node);
    void visit(PrimTypeNode node);
    void visit(StmtNode node);
    void visit(ArrTypeNode node);
    void visit(ClassTypeNode node);
    void visit(ExprstmtNode node);
    void visit(BlockstmtNode node);
    void visit(BreakstmtNode node);
    void visit(ContinuestmtNode node);
    void visit(IfstmtNode node);
    void visit(WhilestmtNode node);
    void visit(ForstmtNode node);
    void visit(DefvarstmtNode node);
    void visit(ReturnstmtNode node);
    void visit(TypeNode node);

    void visit(IdexprNode node);
    void visit(LiterexprNode node);
    void visit(BinaryexprNode node);
    void visit(ArrexprNode node);
    void visit(AssignexprNode node);
    void visit(UnaryexprNode node);
    void visit(MemberexprNode node);
    void visit(FuncallexprNode node);
    void visit(NewexprNode node);
}
