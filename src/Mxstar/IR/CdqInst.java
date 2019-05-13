package Mxstar.IR;

import java.util.HashMap;
import java.util.LinkedList;

public class CdqInst extends IRInstruction {
    public CdqInst(BasicBlock bb) {
        super(bb);
    }
    @Override
    public void renameUseReg(HashMap<Register, Register> renameMap) { }

    @Override
    public void renameDefReg(HashMap<Register, Register> renameMap) { }

    @Override
    public LinkedList<Register> getDefRegs() { return new LinkedList<>(); }

    @Override
    public LinkedList<Register> getUseRegs() { return new LinkedList<>(); }

    @Override
    public LinkedList<StackSlot> getStackSlots() { return new LinkedList<>(); }

    @Override
    public void accept(IRVisitor visitor) {
        visitor.visit(this);
    }
}
