package Mxstar.IR;

import java.util.HashMap;
import java.util.LinkedList;

public class PopInst extends IRInstruction {
    public Address dest;

    public PopInst(BasicBlock bb, Address dest) {
        super(bb);
        this.dest = dest;
    }

    @Override
    public LinkedList<Register> getUseRegs() {
        LinkedList<Register> regs = new LinkedList<>();
        if(dest instanceof Memory)
            regs.addAll(((Memory) dest).getUseRegs());
        return regs;
    }

    @Override
    public LinkedList<StackSlot> getStackSlots() {
        return defaultGetStackSlots(dest);
    }

    @Override
    public void renameUseReg(HashMap<Register, Register> renameMap) {
        if(dest instanceof Memory) {
            dest = ((Memory) dest).copy();
            ((Memory) dest).renameUseReg(renameMap);
        }
    }

    @Override
    public void renameDefReg(HashMap<Register, Register> renameMap) {
        if(dest instanceof Register && renameMap.containsKey(dest))
            dest = renameMap.get(dest);
    }

    @Override
    public LinkedList<Register> getDefRegs() {
        LinkedList<Register> regs = new LinkedList<>();
        if(dest instanceof Register)
            regs.add((Register) dest);
        return regs;
    }

    @Override
    public void accept(IRVisitor visitor) {
        visitor.visit(this);
    }
}
