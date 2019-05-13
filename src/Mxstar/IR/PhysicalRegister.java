package Mxstar.IR;

public class PhysicalRegister extends Register {
    public String name;

    @Override
    public void accept(IRVisitor visitor) {
        visitor.visit(this);
    }
}
