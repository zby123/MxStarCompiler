package Mxstar.IR;

import java.util.HashMap;
import java.util.LinkedList;

public class MoveInst extends IRInstruction {
    public Address dest;
    public Operand src;

    public MoveInst(BasicBlock bb, Address dest, Operand src) {
        super(bb);
        this.dest = dest;
        this.src = src;
        if(dest == null || src == null)
            System.out.println("DARRELL");
    }

    @Override
    public LinkedList<Register> getUseRegs() {
        LinkedList<Register> regs = new LinkedList<>();
        if(dest instanceof Memory)
            regs.addAll(((Memory) dest).getUseRegs());
        if(src instanceof Memory)
            regs.addAll(((Memory) src).getUseRegs());
        else if(src instanceof Register)
            regs.add((Register) src);
        return regs;
    }

    @Override
    public LinkedList<StackSlot> getStackSlots() {
        return defaultGetStackSlots(src, dest);
    }

    @Override
    public void renameUseReg(HashMap<Register, Register> renameMap) {
        if(src instanceof Register && renameMap.containsKey(src))
            src = renameMap.get(src);
        else if(src instanceof Memory) {
            src = ((Memory) src).copy();
            ((Memory) src).renameUseReg(renameMap);
        }
        if(dest instanceof Memory) {
            dest = ((Memory) dest).copy();
            ((Memory) dest).renameUseReg(renameMap);
        }
        if(dest == null || src == null)
            System.out.println("DARRELL");
    }

    @Override
    public void renameDefReg(HashMap<Register, Register> renameMap) {
        if(dest instanceof Register && renameMap.containsKey(dest))
            dest = renameMap.get(dest);
        if(dest == null || src == null)
            System.out.println("DARRELL");
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
