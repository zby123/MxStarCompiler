package Mxstar.IR;

import Mxstar.ST.VariableSymbol;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.io.*;

public class Function {
    public enum Type {
        External, Library, UserDefined
    }
    public Type type;
    public String name;
    public boolean ret;
    public boolean hasOutput;
    public BasicBlock enterBB;
    public BasicBlock leaveBB;
    public LinkedList<BasicBlock> basicblocks;
    public LinkedList<BasicBlock> Rev;
    public LinkedList<BasicBlock> Rev_CFG;
    public LinkedList<VirtualRegister> parameters;

    public HashSet<Function> callee;

    private HashSet<BasicBlock> visitedBasicBlocks;
    private HashSet<Function> visitedFunctions;

    public Function(Type type, String name, boolean ret) {
        this.type = type;
        this.name = name;
        this.GV = new HashSet<>();
        this.recGV = new HashSet<>();
        this.PR = new HashSet<>();
        this.recPR = new HashSet<>();
        this.callee = new HashSet<>();
        this.ret = ret;
        this.hasOutput = false;
        this.basicblocks = new LinkedList<>();
        this.Rev = new LinkedList<>();
        this.Rev_CFG = new LinkedList<>();
        this.parameters = new LinkedList<>();
        this.visitedBasicBlocks = new HashSet<>();
        this.visitedFunctions = new HashSet<>();
        if(type != Type.UserDefined && !name.equals("init")) {
            for(PhysicalRegister pr : RegisterSet.allRegs) {
                if(pr.name.equals("rbp") || pr.name.equals("rsp"))
                    continue;
                this.PR.add(pr);
                this.recPR.add(pr);
            }
        }
    }

    public HashSet<VariableSymbol> GV;
    public HashSet<VariableSymbol> recGV;
    public HashSet<PhysicalRegister> PR;
    public HashSet<PhysicalRegister> recPR;

    private void calcRecPR(Function node) {
        if(visitedFunctions.contains(node)) return;
        visitedFunctions.add(node);
        for(Function func : node.callee)
            calcRecGV(func);
        recPR.addAll(node.PR);
    }

    private void calcRecGV(Function node) {
        if(visitedFunctions.contains(node)) return;
        visitedFunctions.add(node);
        for(Function func : node.callee)
            calcRecGV(func);
        recGV.addAll(node.GV);
    }

    private void calcRev(BasicBlock node) {
        if(visitedBasicBlocks.contains(node)) return;
        visitedBasicBlocks.add(node);
        for(BasicBlock bb : node.successors)
            calcRev(bb);
        Rev.addFirst(node);
    }

    private void calcRev_CFG(BasicBlock node) {
        if(visitedBasicBlocks.contains(node)) return;
        visitedBasicBlocks.add(node);
        for(BasicBlock bb : node.frontiers) {
            calcRev_CFG(bb);
        }
        Rev_CFG.addFirst(node);
    }

    public void finishBuild() {
        for(BasicBlock bb : basicblocks) {
            bb.successors.clear();
            bb.frontiers.clear();
        }
        for(BasicBlock bb : basicblocks) {
            if(bb.tail instanceof CJumpInst) {
                bb.successors.add(((CJumpInst) bb.tail).thenBB);
                bb.successors.add(((CJumpInst) bb.tail).elseBB);
            } else if(bb.tail instanceof JumpInst){
                bb.successors.add(((JumpInst) bb.tail).targetBB);
            }
            for(BasicBlock suc : bb.successors) {
                suc.frontiers.add(bb);
            }
        }

        for(BasicBlock bb : basicblocks) {
            if(bb.tail instanceof CJumpInst) {
                CJumpInst cJumpInst = (CJumpInst)bb.tail;
                if(cJumpInst.thenBB.frontiers.size() < cJumpInst.elseBB.frontiers.size()) {
                    cJumpInst.op = cJumpInst.getNegativeCompareOp();
                    BasicBlock temp = cJumpInst.thenBB;
                    cJumpInst.thenBB = cJumpInst.elseBB;
                    cJumpInst.elseBB = temp;
                }
            }
        }

        visitedBasicBlocks.clear();
        Rev.clear();
        calcRev(enterBB);

        visitedBasicBlocks.clear();
        Rev_CFG.clear();
        calcRev_CFG(leaveBB);

        visitedFunctions.clear();
        recGV.clear();
        calcRecGV(this);
    }

    private LinkedList<PhysicalRegister> trans(LinkedList<Register> regs) {
        LinkedList<PhysicalRegister> ret = new LinkedList<>();
        for(Register r : regs) {
            ret.add((PhysicalRegister) r);
        }
        return ret;
    }

    private boolean useRax(BinaryInst.BinaryOp op) {
        return op == BinaryInst.BinaryOp.MUL || op == BinaryInst.BinaryOp.DIV || op == BinaryInst.BinaryOp.MOD;
    }
    public void finishAllocate() {
        for(BasicBlock bb : basicblocks) {
            for(IRInstruction inst = bb.head; inst != null; inst = inst.next) {
                if(inst instanceof ReturnInst)
                    continue;
                if(inst instanceof CallInst) {
                    PR.addAll(RegisterSet.callerSave);
                } else if(inst instanceof BinaryInst && useRax(((BinaryInst) inst).op)) {
                    if(((BinaryInst) inst).src instanceof Register)
                        PR.add((PhysicalRegister) ((BinaryInst) inst).src);
                    PR.add(RegisterSet.rax);
                    PR.add(RegisterSet.rdx);
                } else {
                    PR.addAll(trans(inst.getUseRegs()));
                    PR.addAll(trans(inst.getDefRegs()));
                }
            }
        }
        visitedFunctions.clear();
        calcRecPR(this);
    }

    public void accept(IRVisitor visitor) {
        visitor.visit(this);
    }
}
