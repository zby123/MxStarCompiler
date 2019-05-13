package Mxstar.IR;

import java.util.LinkedList;

public class IRProgram {
    public LinkedList<Function> functions;
    public LinkedList<StaticData> staticData;

    public IRProgram() {
        functions = new LinkedList<>();
        staticData = new LinkedList<>();
    }

    public void accept(IRVisitor visitor) {
        visitor.visit(this);
    }
}
