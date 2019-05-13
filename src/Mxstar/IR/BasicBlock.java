package Mxstar.IR;

import java.util.LinkedList;

public class BasicBlock {
    public String hint;
    public Function function;
    public IRInstruction head;
    public IRInstruction tail;

    public LinkedList<BasicBlock> frontiers = null;
    public LinkedList<BasicBlock> successors = null;

    private static int globalBlockId = 0;
    public int blockId;

    public BasicBlock(Function function, String hint) {
        this.function = function;
        this.hint = hint;
        this.frontiers = new LinkedList<>();
        this.successors = new LinkedList<>();
        function.basicblocks.add(this);
        blockId = globalBlockId++;
    }

    public boolean isEnded() {
        return tail instanceof ReturnInst || tail instanceof JumpInst || tail instanceof CJumpInst;
    }

    public void prepend(IRInstruction inst) {
        head.prepend(inst);
    }
    public void append(IRInstruction inst) {
        if(tail instanceof CJumpInst || tail instanceof JumpInst || tail instanceof ReturnInst)
            return;
        if (head == null) {
            inst.prev = inst.next = null;
            head = tail = inst;
        } else {
            tail.append(inst);
        }
    }

    public void accept(IRVisitor visitor) {
        visitor.visit(this);
    }
}
