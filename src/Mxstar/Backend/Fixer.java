package Mxstar.Backend;

import Mxstar.IR.*;
import Mxstar.ST.*;

import java.util.HashSet;
import java.util.LinkedList;

public class Fixer implements IRVisitor {
    IRProgram irProg;

    @Override
    public void visit(IRProgram program) {
        this.irProg = program;
        for(Function f : program.functions)
            f.accept(this);
    }

    @Override
    public void visit(Function function) {
        for(BasicBlock bb : function.basicblocks) {
            bb.accept(this);
        }
    }

    @Override
    public void visit(BasicBlock basicBlock) {
        for(IRInstruction inst = basicBlock.head; inst != null; inst = inst.next)
            inst.accept(this);
    }

    @Override
    public void visit(BinaryInst inst) {
        if((inst.op == BinaryInst.BinaryOp.MUL || inst.op == BinaryInst.BinaryOp.DIV || inst.op == BinaryInst.BinaryOp.MOD)
                && inst.src instanceof Constant) {
            VirtualRegister vr = new VirtualRegister("");
            inst.prepend(new MoveInst(inst.bb, vr, inst.src));
            inst.src = vr;
        }
    }
    
    @Override
    public void visit(MoveInst inst) {
        if(inst.src instanceof Memory && inst.dest instanceof Memory) {
            VirtualRegister vr = new VirtualRegister("");
            inst.prepend(new MoveInst(inst.bb, vr, inst.src));
            inst.src = vr;
        }
    }

    @Override
    public void visit(CJumpInst inst) {
        if(inst.src1 instanceof Constant) {
            if(inst.src2 instanceof Constant) {
                inst.prepend(new JumpInst(inst.bb, inst.doCompare()));
                inst.remove();
            } else {
                Operand tmp = inst.src1;
                inst.src1 = inst.src2;
                inst.src2 = tmp;
                inst.op = inst.getReverseCompareOp();
            }
        }
    }

    @Override
    public void visit(CallInst inst) {
        Function caller = inst.bb.function;
        Function callee = inst.func;
        HashSet<VariableSymbol> callerUsed = caller.GV;
        HashSet<VariableSymbol> calleeUsed = callee.recGV;
        for(VariableSymbol vs : callerUsed) {
            if(calleeUsed.contains(vs)) {
                inst.prepend(new MoveInst(inst.bb, vs.vr.spillPlace, vs.vr));
                inst.prev.accept(this);
            }
        }
        while(inst.args.size() > 6)
            inst.prepend(new PushInst(inst.bb, inst.args.removeLast()));

        for(int i = inst.args.size() - 1; i >= 0; i--) {
            inst.prepend(new MoveInst(inst.bb, RegisterSet.vargs.get(i), inst.args.get(i)));
            inst.prev.accept(this);
        }
        for(VariableSymbol vs : callerUsed) {
            if(calleeUsed.contains(vs)) {
                inst.append(new MoveInst(inst.bb, vs.vr, vs.vr.spillPlace));
            }
        }
    }

    @Override
    public void visit(VirtualRegister operand) {

    }

    @Override
    public void visit(PhysicalRegister operand) {

    }

    @Override
    public void visit(Memory operand) {

    }

    @Override
    public void visit(StackSlot operand) {

    }

    @Override
    public void visit(Constant operand) {

    }

    @Override
    public void visit(Immediate operand) {

    }

    @Override
    public void visit(StaticData operand) {

    }

    @Override
    public void visit(UnaryInst inst) {

    }

    @Override
    public void visit(PushInst inst) {

    }

    @Override
    public void visit(PopInst inst) {

    }

    @Override
    public void visit(JumpInst inst) {

    }

    @Override
    public void visit(LeaInst inst) {

    }

    @Override
    public void visit(ReturnInst inst) {

    }

    @Override
    public void visit(LeaveInst inst) {
    }

    @Override
    public void visit(CdqInst inst) {
    }

    @Override
    public void visit(FunctionAddress operand) {
    }
}
