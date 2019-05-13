package Mxstar.IR;

public class Immediate extends Constant {
    public long value;

    public Immediate(long value) {
        this.value = value;
    }

    @Override
    public void accept(IRVisitor visitor) {
        visitor.visit(this);
    }
}
