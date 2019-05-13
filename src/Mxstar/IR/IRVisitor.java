package Mxstar.IR;

public interface IRVisitor {
    void visit(IRProgram program);
    void visit(Function function);
    void visit(BasicBlock basicBlock);
    void visit(VirtualRegister operand);
    void visit(PhysicalRegister operand);
    void visit(Memory operand);
    void visit(StackSlot operand);
    void visit(Constant operand);
    void visit(Immediate operand);
    void visit(StaticData operand);
    void visit(BinaryInst inst);
    void visit(UnaryInst inst);
    void visit(MoveInst inst);
    void visit(PushInst inst);
    void visit(PopInst inst);
    void visit(CJumpInst inst);
    void visit(JumpInst inst);
    void visit(LeaInst inst);
    void visit(ReturnInst inst);
    void visit(CallInst inst);
    void visit(LeaveInst inst);
    void visit(CdqInst inst);
    void visit(FunctionAddress operand);
}
