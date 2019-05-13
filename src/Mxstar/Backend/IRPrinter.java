package Mxstar.Backend;

import Mxstar.IR.*;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Formatter;
import java.util.HashMap;

public class IRPrinter implements IRVisitor {
    public StringBuilder strBuilder;

    public HashMap<StaticData, String> sdNameMap;
    public HashMap<VirtualRegister, String> vrNameMap;
    public HashMap<BasicBlock, String> bbNameMap;
    public HashMap<StackSlot, String> ssNameMap;
    public int varCnt = 0;
    public int sdCnt = 0;
    public int bbCnt = 0;
    public int ssCnt = 0;

    public BasicBlock nxtBB = null;

    public boolean inLeaInst = false;

    public IRPrinter(){
        this.strBuilder = new StringBuilder();
        this.sdNameMap = new HashMap<>();
        this.vrNameMap = new HashMap<>();
        this.bbNameMap = new HashMap<>();
        this.ssNameMap = new HashMap<>();
    }

    public void printTo(PrintStream out){
        out.print(strBuilder.toString());
    }

    @Override
    public void visit(IRProgram node){
        try{
            BufferedReader br = new BufferedReader(new FileReader("lib/lib.asm"));
            String lineRead;
            while((lineRead = br.readLine()) != null)
                append(lineRead + "\n");
        }
        catch (IOException e){
            e.printStackTrace();
            System.exit(1);
        }
        append(";\n");
        append("\tsection\t.text\n");

        for (Function f : node.functions)
            f.accept(this);

        append("\tsection\t.data\n");
        for (StaticData sd : node.staticData){
            append(getSDName(sd));
            append(":\n");

            if (sd.init == null){
                append("\tdb ");
                for (int i = 0; i < sd.bytes; ++i){
                    if (i != 0)
                        append(", ");
                    append("00H");
                }
                append("\n");
            }
            else {
                append("\tdq " + sd.init.length() + "\n");
                append("\tdb ");
                for (int i = 0; i < sd.init.length(); ++i) {
                    Formatter ft = new Formatter();
                    ft.format("%02XH, ", (int) sd.init.charAt(i));
                    append(ft.toString());
                }
                append("00H\n");
            }
        }
    }

    @Override
    public void visit(Function node){
        append(getFuncName((node)));
        append(":\n");
        ArrayList<BasicBlock> rpo = new ArrayList<>(node.Rev);
        for (int i = 0; i < rpo.size(); ++i){
            BasicBlock bb = rpo.get(i);
            if (i == rpo.size() -1)
                nxtBB = null;
            else nxtBB = rpo.get(i+1);
            bb.accept(this);
        }
    }

    @Override
    public void visit(BasicBlock node){
        append("\t" + getBBName(node));
        append(":\n");
        for (IRInstruction i = node.head; i != null; i = i.next)
            i.accept(this);
    }
    @Override
    public void visit(CJumpInst node) {
        String cop = null;
        switch (node.op) {
            case E:
                cop = "je";
                break;
            case NE:
                cop = "jne";
                break;
            case G:
                cop = "jg";
                break;
            case L:
                cop = "jl";
                break;
            case GE:
                cop = "jge";
                break;
            case LE:
                cop = "jle";
                break;
        }
        append("\tcmp ");
        node.src1.accept(this);
        append(", ");
        node.src2.accept(this);
        append("\n");
        append("\t" + cop + " " + getBBName(node.thenBB) + "\n");
        if (node.elseBB != nxtBB) {
            append("\tjmp " + getBBName(node.elseBB)+ "\n");
        }
    }

    @Override
    public void visit(JumpInst node){
        if (node.targetBB == nxtBB)
            return;
        append("\tjmp " + getBBName(node.targetBB) + "\n");

    }

    @Override
    public void visit(ReturnInst node){
        append("\tret \n");
    }

    @Override
    public void visit(UnaryInst node){
        String uop = null;
        switch (node.op){
            case NOT:
                uop = "not";
                break;
            case NEG:
                uop = "neg";
                break;
            case INC:
                uop = "inc";
                break;
            case DEC:
                uop = "dec";
                break;
        }
        append("\t" + uop + " ");
        node.dest.accept(this);
        append("\n");

    }

    @Override
    public void visit(BinaryInst node){
        if (node.op == BinaryInst.BinaryOp.MUL){
            append("\timul ");
            node.src.accept(this);
            append("\n");
            return;
        }
        if (node.op == BinaryInst.BinaryOp.DIV || node.op == BinaryInst.BinaryOp.MOD){
            append("\tidiv ");
            node.src.accept(this);
            append("\n");
            return;
        }
        if (node.op == BinaryInst.BinaryOp.SAL){
            append("\tsal ");
            node.dest.accept(this);
            append(", cl\n");
            return;
        }
        if (node.op == BinaryInst.BinaryOp.SAR){
            append("\tsar ");
            node.dest.accept(this);
            append(", cl\n");
            return;
        }

        String bop = null;
        switch (node.op){
            case ADD:
                bop = "add";
                break;
            case SUB:
                bop = "sub";
                break;
            case AND:
                bop = "and";
                break;
            case OR:
                bop = "or";
                break;
            case XOR:
                bop = "xor";
                break;
        }
        append("\t" + bop + " ");
        node.dest.accept(this);
        append(", ");
        node.src.accept(this);
        append("\n");
    }

    @Override
    public void visit(MoveInst node){
        if (node.src == node.dest)
            return;
        append("\tmov ");
        node.dest.accept(this);
        append(", ");
        node.src.accept(this);
        append("\n");
    }

    @Override
    public void visit(CallInst node){
        append("\tcall " + getFuncName(node.func) + "\n");
    }

    @Override
    public void visit(PushInst node){
        append("\tpush ");
        node.src.accept(this);
        append("\n");
    }

    @Override
    public void visit(PopInst node){
        append("\tpop ");
        node.dest.accept(this);
        append("\n");
    }

    @Override
    public void visit(LeaInst node){
        inLeaInst = true;
        append("\tlea ");
        node.dest.accept(this);
        append(", ");
        node.src.accept(this);
        append("\n");
        inLeaInst = false;
    }

    @Override
    public void visit(CdqInst node){
        append("\tcdq \n");
    }

    @Override
    public void visit(LeaveInst node){
        append("\tleave \n");
    }

    @Override
    public void visit(VirtualRegister node){
        assert false;
    }

    @Override
    public void visit(PhysicalRegister node){
        append(node.name);
    }

    @Override
    public void visit(Memory node){
        boolean flag =false;
        if (!inLeaInst)
            append("qword");
        append("[");

        if (node.base != null){
            node.base.accept(this);
            flag = true;
        }

        if (node.index != null){
            if (flag)
                append(" + ");
            node.index.accept(this);
            if (node.scale != 1)
                append(" * " + node.scale);
            flag = true;
        }

        if (node.constant != null){
            if (node.constant instanceof StaticData){
                if (flag)
                    append(" + ");
                node.constant.accept(this);
            }
            else if (node.constant instanceof Immediate){
                long val = ((Immediate) node.constant).value;
                if (flag){
                    if (val > 0)
                        append(" + " + val);
                    if (val < 0)
                        append(" - " + (-val));
                }
                else append(String.valueOf(val));
            }
        }

        append("]");
    }

    @Override
    public void visit(StackSlot node){
        if (node.base == null && node.index == null && node.constant == null)
            assert false;
        else visit((Memory)node);
    }

    @Override
    public void visit(Immediate node){
        append(String.valueOf(node.value));
    }

    @Override
    public void visit(StaticData node){
        append(getSDName(node));
    }

    @Override
    public void visit(FunctionAddress node){
        append(getFuncName(node.function));
    }

    private void append(String str){
        strBuilder.append(str);
    }

    private String getBBName(BasicBlock bb){
        if (bbNameMap.containsKey(bb))
            return bbNameMap.get(bb);
        bbNameMap.put(bb, "bb_"+ (bbCnt++));
        return bbNameMap.get(bb);
    }

    private String getVRName(VirtualRegister vr){
        if (vrNameMap.containsKey(vr))
            return vrNameMap.get(vr);
        vrNameMap.put(vr, "vr_"+ (varCnt++) + "<" + vr.hint + ">");
        return vrNameMap.get(vr);
    }

    private String getSDName(StaticData sd){
        if (sdNameMap.containsKey(sd))
            return sdNameMap.get(sd);
        sdNameMap.put(sd, "sd_" + (sdCnt++));
        return sdNameMap.get(sd);
    }

    private String getSSName(StackSlot ss){
        if (ssNameMap.containsKey(ss))
            return ssNameMap.get(ss);
        ssNameMap.put(ss, "ss[" + (ssCnt++)+ "]");
        return ssNameMap.get(ss);
    }

    private String getFuncName(Function f){
        if (f.name.equals("malloc"))
            return f.name; //externalMalloc
        if (f.type==Function.Type.UserDefined)
            return "_" + f.name;
        return "lib_" + f.name; //lib
    }

    @Override
    public void visit(Constant operand) {
        assert false;
    }

}
