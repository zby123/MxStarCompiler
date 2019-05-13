package Mxstar.IR;

public class StackSlot extends Memory {
    public Function function;
    public String hint;

    public StackSlot(String hint) {
        this.hint = hint;
    }
    public void accept(IRVisitor visitor) {
        visitor.visit(this);
    }
}
