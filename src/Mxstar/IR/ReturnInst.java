package Mxstar.IR;
import Mxstar.Backend.IRbuilder;

import java.util.HashMap;
import java.util.LinkedList;

public class ReturnInst extends IRInstruction {

    public ReturnInst(BasicBlock bb) {
        super(bb);
    }

    @Override
    public void renameUseReg(HashMap<Register, Register> renameMap) { }

    @Override
    public void renameDefReg(HashMap<Register, Register> renameMap) { }

    @Override
    public LinkedList<Register> getDefRegs() { return new LinkedList<>(); }

    @Override
    public LinkedList<Register> getUseRegs() {
        LinkedList<Register> regs = new LinkedList<>();
        if(bb.function.ret)
            regs.add(RegisterSet.vrax);
        return regs;
    }

    @Override
    public LinkedList<StackSlot> getStackSlots() { return new LinkedList<>(); }

    @Override
    public void accept(IRVisitor visitor) {
        visitor.visit(this);
    }
}
