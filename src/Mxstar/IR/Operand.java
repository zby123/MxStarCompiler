package Mxstar.IR;

import java.util.HashMap;
import java.util.LinkedList;

public abstract class Operand {
    public abstract void accept(IRVisitor visitor);
}
