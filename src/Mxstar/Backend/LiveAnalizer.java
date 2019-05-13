package Mxstar.Backend;

import Mxstar.IR.*;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import static java.lang.System.exit;

public class LiveAnalizer {

    public static class Graph {
        HashMap<VirtualRegister,HashSet<VirtualRegister>> graph;

        Graph() {
            graph = new HashMap<>();
        }
        Graph(Graph g) {
            graph = new HashMap<>();
            for(VirtualRegister reg : g.getAllRegisters())
                graph.put(reg, new HashSet<>(g.getAdjacents(reg)));
        }
        void addRegiser(VirtualRegister vr) {
            if(graph.containsKey(vr)) return;
            graph.put(vr, new HashSet<>());
        }
        void addRegisers(Collection<VirtualRegister> vrs) {
            for(VirtualRegister reg : vrs)
                addRegiser(reg);
        }
        void addEdge(VirtualRegister a, VirtualRegister b) {
            if(a == b) return;
            graph.get(a).add(b);
            graph.get(b).add(a);
        }
        void delEdge(VirtualRegister a, VirtualRegister b) {
            if(graph.containsKey(a) && graph.get(a).contains(b)) {
                graph.get(a).remove(b);
                graph.get(b).remove(a);
            }
        }
        void delRegister(VirtualRegister vr) {
            for(VirtualRegister reg : getAdjacents(vr))
                graph.get(reg).remove(vr);
            graph.remove(vr);
        }
        int getDegree(VirtualRegister a) {
            return graph.containsKey(a) ? graph.get(a).size() : 0;
        }
        boolean isLinked(VirtualRegister a, VirtualRegister b) {
            return graph.containsKey(a) && graph.get(a).contains(b);
        }
        void clear() {
            graph.clear();
        }
        void forEach(BiConsumer<VirtualRegister,VirtualRegister> consumer) {
            for(VirtualRegister reg1 : graph.keySet())
                for(VirtualRegister reg2 : graph.get(reg1))
                    consumer.accept(reg1, reg2);
        }
        Collection<VirtualRegister> getAdjacents(VirtualRegister a) {
            return graph.getOrDefault(a, new HashSet<>());
        }
        Collection<VirtualRegister> getAllRegisters() {
            return graph.keySet();
        }

    }
    public HashMap<BasicBlock, HashSet<VirtualRegister>> liveOut;
    public HashMap<BasicBlock, HashSet<VirtualRegister>> usedRegisters;
    public HashMap<BasicBlock, HashSet<VirtualRegister>> definedRegisters;

    private void init(Function function) {
        liveOut = new HashMap<>();
        usedRegisters = new HashMap<>();
        definedRegisters = new HashMap<>();
        for(BasicBlock bb : function.basicblocks) {
            liveOut.put(bb, new HashSet<>());
            usedRegisters.put(bb, new HashSet<>());
            definedRegisters.put(bb, new HashSet<>());
        }
    }

    private void initRegs(BasicBlock bb, boolean flag) {
        HashSet<VirtualRegister> bbUsedRegisters = new HashSet<>();
        HashSet<VirtualRegister> bbDefinedRegisters = new HashSet<>();
        for(IRInstruction inst = bb.head; inst != null; inst = inst.next) {
            LinkedList<Register> usedRegs;
            if(inst instanceof CallInst && !flag)
                usedRegs = ((CallInst) inst).getCallInstUsed();
            else
                usedRegs = inst.getUseRegs();
            for(VirtualRegister reg : trans(usedRegs))
                if(!bbDefinedRegisters.contains(reg))
                    bbUsedRegisters.add(reg);
            bbDefinedRegisters.addAll(trans(inst.getDefRegs()));
        }
        definedRegisters.put(bb, bbDefinedRegisters);
        usedRegisters.put(bb, bbUsedRegisters);
    }

    private boolean isMove(IRInstruction inst) {
        if(inst instanceof MoveInst) {
            MoveInst move = (MoveInst)inst;
            return move.dest instanceof VirtualRegister && move.src instanceof VirtualRegister;
        } else {
            return false;
        }
    }

    public LinkedList<VirtualRegister> trans(Collection<Register> registers) {
        LinkedList<VirtualRegister> virtualRegisters = new LinkedList<>();
        for(Register reg : registers) {
            virtualRegisters.add((VirtualRegister) reg);
        }
        return virtualRegisters;
    }

    private void calcLive(Function function, boolean flag) {
        init(function);

        for(BasicBlock bb : function.basicblocks)
            initRegs(bb, flag);

        boolean changed = true;
        while(changed) {
            changed = false;
            LinkedList<BasicBlock> basicBlocks = function.Rev;
            for(BasicBlock bb : basicBlocks) {
                int oldSize = liveOut.get(bb).size();
                for(BasicBlock succ : bb.successors) {
                    HashSet<VirtualRegister> regs = new HashSet<>(liveOut.get(succ));
                    regs.removeAll(definedRegisters.get(succ));
                    regs.addAll(usedRegisters.get(succ));
                    liveOut.get(bb).addAll(regs);
                }
                changed = changed || liveOut.get(bb).size() != oldSize;
            }
        }
    }

    public HashMap<BasicBlock,HashSet<VirtualRegister>> getLiveOut(Function function) {
        calcLive(function, false);
        return liveOut;
    }

    public void getInferenceGraph(Function function, Graph graph, Graph moveGraph) {
        calcLive(function, true);

        graph.clear();
        if(moveGraph != null)
            moveGraph.clear();

        for(BasicBlock bb : function.basicblocks) {
            for(IRInstruction inst = bb.head; inst != null; inst = inst.next) {
                graph.addRegisers(trans(inst.getDefRegs()));
                graph.addRegisers(trans(inst.getUseRegs()));
            }
        }

        for(BasicBlock bb : function.basicblocks) {
            HashSet<VirtualRegister> liveNow = new HashSet<>(liveOut.get(bb));
            for(IRInstruction inst = bb.tail; inst != null; inst = inst.prev) {
                boolean isMBR = isMove(inst);
                for(VirtualRegister reg1 : trans(inst.getDefRegs())) {
                    for(VirtualRegister reg2 : liveNow) {
                        if(isMBR && moveGraph != null && ((MoveInst)inst).src == reg1) {
                            moveGraph.addEdge(reg1, reg2);
                            continue;
                        }
                        graph.addEdge(reg1, reg2);
                    }
                }
                liveNow.removeAll(trans(inst.getDefRegs()));
                liveNow.addAll(trans(inst.getUseRegs()));
            }
        }

        /* remove some invalid <reg,reg> in move graph */
        if(moveGraph != null) {
            graph.forEach(moveGraph::delEdge);
        }
    }
}
