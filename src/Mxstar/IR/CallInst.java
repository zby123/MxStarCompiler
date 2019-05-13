package Mxstar.IR;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;

public class CallInst extends IRInstruction {
    public Address dest;
    public Function func;
    public LinkedList<Operand> args;

    private void update() {
        Function caller = super.bb.function;
        caller.callee.add(func);
        if(func.name.equals("println") || func.name.equals("print"))
            super.bb.function.hasOutput = true;
    }
    public CallInst(BasicBlock bb, Address dest, Function func, Operand... args) {
        super(bb);
        this.dest = dest;
        this.func = func;
        this.args = new LinkedList<>(Arrays.asList(args));
        update();
    }
    public CallInst(BasicBlock bb, Address dest, Function func, LinkedList<Operand> args) {
        super(bb);
        this.dest = dest;
        this.func = func;
        this.args = new LinkedList<>(args);
        update();
    }

    public LinkedList<Register> getCallInstUsed() {
        LinkedList<Register> registers = new LinkedList<>();
        for(Operand operand : args) {
            if(operand instanceof Memory) {
                registers.addAll(((Memory) operand).getUseRegs());
            } else if(operand instanceof VirtualRegister) {
                registers.add((Register) operand);
            }
        }
        return registers;
    }

    @Override
    public LinkedList<Register> getUseRegs() {
        return new LinkedList<>(RegisterSet.vargs.subList(0, Integer.min(6, args.size())));
    }

    @Override
    public LinkedList<Register> getDefRegs() {
        LinkedList<Register> regs = new LinkedList<>(RegisterSet.vcallerSave);
        return regs;
    }

    @Override
    public LinkedList<StackSlot> getStackSlots() {
        LinkedList<StackSlot> slots = new LinkedList<>();
        slots.addAll(defaultGetStackSlots(dest));
        for(Operand operand : args) {
            if(operand instanceof StackSlot)
                slots.add((StackSlot) operand);
        }
        return slots;
    }

    @Override
    public void renameUseReg(HashMap<Register, Register> renameMap) {
    }

    @Override
    public void renameDefReg(HashMap<Register, Register> renameMap) {
        if(dest instanceof Register && renameMap.containsKey(dest))
            dest = renameMap.get(dest);
    }


    @Override
    public void accept(IRVisitor visitor) {
        visitor.visit(this);
    }
}
