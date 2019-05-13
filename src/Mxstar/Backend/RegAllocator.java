package Mxstar.Backend;

import Mxstar.IR.*;
import Mxstar.Backend.LiveAnalizer.Graph;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;

public class RegAllocator {
    private IRProgram irProg;
    private static LiveAnalizer LA = new LiveAnalizer();
    private LinkedList<PhysicalRegister> P_RegSet = new LinkedList<>();
    private int K;

    Function function;
    Graph orignGraph;
    Graph graph;
    LinkedList<VirtualRegister> selectStack;
    HashMap<VirtualRegister, PhysicalRegister> colors;


    public RegAllocator(IRProgram irProg) {
        this.irProg = irProg;
        for(PhysicalRegister pr : RegisterSet.allRegs) {
            if(pr.name.equals("rsp") || pr.name.equals("rbp"))
                continue;
            P_RegSet.add(pr);
        }
        K = P_RegSet.size();
    }
    
    private void work() {
        orignGraph = new Graph();
        while(true) {

            LA.getInferenceGraph(function, orignGraph, null);
            graph = new Graph(orignGraph);
            init();
            do {
                if (!retToSimplify.isEmpty()) simplify();
                else if (!regToSpill.isEmpty()) spill();
            } while (!retToSimplify.isEmpty() || !regToSpill.isEmpty());
            assignColors();
            if (!regSpilled.isEmpty())
                reconstructFunc();
            else {
                replaceRegs();
                break;
            }
        }

        function.finishAllocate();
    }

    public void run() {
        for(Function function : irProg.functions) {
            this.function = function;
            work();
        }
    }
    HashSet<VirtualRegister> retToSimplify;
    HashSet<VirtualRegister> regToSpill;
    HashSet<VirtualRegister> regSpilled;

    private LinkedList<VirtualRegister> trans(LinkedList<Register> regs) {
        LinkedList<VirtualRegister> ret = new LinkedList<>();
        for(Register r : regs) {
            ret.add((VirtualRegister)r);
        }
        return ret;
    }

    private void init() {
        retToSimplify = new HashSet<>();
        regToSpill = new HashSet<>();
        regSpilled = new HashSet<>();
        selectStack = new LinkedList<>();
        colors = new HashMap<>();
        for(VirtualRegister vr : graph.getAllRegisters()) {
            if(graph.getDegree(vr) < K)
                retToSimplify.add(vr);
            else
                regToSpill.add(vr);
        }
    }
    private void simplify() {
        VirtualRegister reg = retToSimplify.iterator().next();
        LinkedList<VirtualRegister> neighbors = new LinkedList<>(graph.getAdjacents(reg));
        graph.delRegister(reg);
        for(VirtualRegister vr : neighbors) {
            if(graph.getDegree(vr) < K && regToSpill.contains(vr)) {
                regToSpill.remove(vr);
                retToSimplify.add(vr);
            }
        }
        retToSimplify.remove(reg);
        selectStack.addFirst(reg);
    }
    private void spill() {
        VirtualRegister candidate = null;
        int rank = -2;
        for(VirtualRegister vr : regToSpill) {
            int curRank = graph.getDegree(vr);
            if(vr.allocatedPhysicalRegister != null)
                curRank = -1;
            if(curRank > rank) {
                candidate = vr;
                rank = curRank;
            }
        }
        graph.delRegister(candidate);
        regToSpill.remove(candidate);
        selectStack.addFirst(candidate);
    }
    private void assignColors() {
        for(VirtualRegister vr : selectStack) {
            if(vr.allocatedPhysicalRegister != null)
                colors.put(vr, vr.allocatedPhysicalRegister);
        }
        for(VirtualRegister vr : selectStack) {
            if(vr.allocatedPhysicalRegister != null)
                continue;
            HashSet<PhysicalRegister> okColors = new HashSet<>(P_RegSet);
            for(VirtualRegister neighbor : orignGraph.getAdjacents(vr)) {
                if(colors.containsKey(neighbor))
                    okColors.remove(colors.get(neighbor));
            }
            if(okColors.isEmpty()) {
                regSpilled.add(vr);
            } else {
                PhysicalRegister pr = null;
                for(PhysicalRegister reg : RegisterSet.callerSave) {
                    if (okColors.contains(reg)) {
                        pr = reg;
                        break;
                    }
                }
                if(pr == null)
                    pr = okColors.iterator().next();
                colors.put(vr, pr);
            }
        }
    }
    private void reconstructFunc() {
        HashMap<VirtualRegister, Memory> spillPlaces = new HashMap<>();
        for(VirtualRegister vr : regSpilled) {
            if(vr.spillPlace != null) {
                spillPlaces.put(vr,  vr.spillPlace);
            } else {
                spillPlaces.put(vr, new StackSlot(vr.hint));
            }
        }
        for(BasicBlock bb : function.basicblocks) {
            for(IRInstruction inst = bb.head; inst != null; inst = inst.next) {
                LinkedList<VirtualRegister> used = new LinkedList<>(trans(inst.getUseRegs()));
                LinkedList<VirtualRegister> defined = new LinkedList<>(trans(inst.getDefRegs()));
                HashMap<Register,Register> renameMap = new HashMap<>();
                used.retainAll(regSpilled);
                defined.retainAll(regSpilled);
                for(VirtualRegister reg : used)
                    if(!renameMap.containsKey(reg)) renameMap.put(reg, new VirtualRegister(""));
                for(VirtualRegister reg : defined)
                    if(!renameMap.containsKey(reg)) renameMap.put(reg, new VirtualRegister(""));
                inst.renameDefReg(renameMap);
                inst.renameUseReg(renameMap);
                for(VirtualRegister reg : used)
                    inst.prepend(new MoveInst(inst.bb, renameMap.get(reg), spillPlaces.get(reg)));
                for(VirtualRegister reg : defined) {
                    inst.append(new MoveInst(inst.bb, spillPlaces.get(reg), renameMap.get(reg)));
                    inst = inst.next;
                }
            }
        }
    }
    private void replaceRegs() {
        HashMap<Register,Register> renameMap = new HashMap<>();
        for(HashMap.Entry<VirtualRegister,PhysicalRegister> entry : colors.entrySet())
            renameMap.put(entry.getKey(), entry.getValue());
        for(BasicBlock bb : function.basicblocks)
            for(IRInstruction inst = bb.head; inst != null; inst = inst.next) {
                inst.renameUseReg(renameMap);
                inst.renameDefReg(renameMap);
            }
    }

}
