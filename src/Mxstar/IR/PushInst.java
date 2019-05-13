package Mxstar.IR;

import java.util.HashMap;
import java.util.LinkedList;

public class PushInst extends IRInstruction {
    public Operand src;

    public PushInst(BasicBlock bb, Operand src) {
        super(bb);
        this.src = src;
    }

    @Override
    public LinkedList<Register> getUseRegs() {
        LinkedList<Register> regs = new LinkedList<>();
        if(src instanceof Memory)
            regs.addAll(((Memory) src).getUseRegs());
        return regs;
    }

    @Override
    public LinkedList<StackSlot> getStackSlots() {
        return defaultGetStackSlots(src);
    }

    @Override
    public void renameUseReg(HashMap<Register, Register> renameMap) {
        if(src instanceof Memory) {
            src = ((Memory) src).copy();
            ((Memory) src).renameUseReg(renameMap);
        }
    }

    @Override
    public void renameDefReg(HashMap<Register, Register> renameMap) {
        if(src instanceof Register && renameMap.containsKey(src))
            src = renameMap.get(src);
    }

    @Override
    public LinkedList<Register> getDefRegs() {
        LinkedList<Register> regs = new LinkedList<>();
        if(src instanceof Register)
            regs.add((Register) src);
        return regs;
    }

    @Override
    public void accept(IRVisitor visitor) {
        visitor.visit(this);
    }
}
