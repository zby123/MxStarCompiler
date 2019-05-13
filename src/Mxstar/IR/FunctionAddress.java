package Mxstar.IR;

public class FunctionAddress extends Constant{
    public Function function;

    public FunctionAddress(Function function) {
        this.function = function;
    }

    @Override
    public void accept(IRVisitor visitor) {
        visitor.visit(this);
    }
}
