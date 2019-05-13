package Mxstar.IR;

public class StaticData extends Constant {
    public String hint;
    public int bytes;
    public String init;

    public StaticData(String hint, int bytes) {
        this.hint = hint;
        this.bytes = bytes;
        this.init = null;
    }
    public StaticData(String hint, String init) {
        this.hint = hint;
        this.bytes = init.length() + 9;
        this.init = init;
    }

    @Override
    public void accept(IRVisitor visitor) {
        visitor.visit(this);
    }
}
