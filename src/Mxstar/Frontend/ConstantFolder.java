package Mxstar.Frontend;

import Mxstar.AST.*;

import java.util.*;
import java.io.*;

public class ConstantFolder implements ASTVisitor {

    private HashMap<ExprNode, LiterexprNode> toConst;

    public ConstantFolder() {
        toConst = new HashMap<>();
    }

    @Override public void visit(ASTProgram node) {
        for (DefunNode d : node.functions) d.accept(this);
        for (DefclassNode d : node.classes) d.accept(this);
        for (DefvarNode d : node.vars) d.accept(this);
    }

    @Override public void visit(DefNode node) {
        assert false;
    }

    @Override public void visit(DefunNode node) {
        for (DefvarNode d : node.paramList)
            d.accept(this);
        for (StmtNode d : node.funcBody)
            d.accept(this);
    }

    @Override public void visit(DefvarNode node) {
        if (node.init != null) {
            node.init.accept(this);
            if (toConst.get(node.init) != null) {
                node.init = toConst.get(node.init);
            }
        }
    }

    @Override public void visit(DefclassNode node) {

    }

    @Override public void visit(ArrTypeNode node) {
        assert false;
    }

    @Override public void visit(TypeNode node) {
        assert false;
    }

    @Override public void visit(ClassTypeNode node) {
        assert false;
    }

    @Override public void visit(PrimTypeNode node) {
        assert false;
    }

    @Override public void visit(StmtNode node) {
        assert false;
    }

    @Override public void visit(BlockstmtNode node) {
        for (StmtNode s : node.stmts) {
            s.accept(this);
        }
    }

    @Override public void visit(ExprNode node) {
        assert false;
    }

    @Override public void visit(ExprstmtNode node) {
        node.expr.accept(this);
        if (toConst.get(node.expr) != null) {
            node.expr = toConst.get(node.expr);
        }
    }

    @Override public void visit(IfstmtNode node) {
        node.condition.accept(this);
        if (toConst.get(node.condition) != null) {
            node.condition = toConst.get(node.condition);
        }
        if (node.body != null) node.body.accept(this);
        if (node.elseBody != null) node.elseBody.accept(this);
    }

    @Override public void visit(WhilestmtNode node) {
        node.condition.accept(this);
        if (toConst.get(node.condition) != null) {
            node.condition = toConst.get(node.condition);
        }
        if (node.body != null) node.body.accept(this);
    }

    @Override public void visit(ForstmtNode node) {
        if (node.initCond != null) {
            node.initCond.accept(this);
        }
        if (node.endCond != null) {
            node.endCond.accept(this);
            if (toConst.get(node.endCond) != null) 
                node.endCond = toConst.get(node.endCond);
        }
        if (node.upd != null) {
            node.upd.accept(this);
        }
        if (node.body != null) 
            node.body.accept(this);
    }

    @Override public void visit(BreakstmtNode node) {
        assert false;
    }

    @Override public void visit(ContinuestmtNode node) {
        assert false;
    }

    @Override public void visit(ReturnstmtNode node) {
        if (node.retVal != null) {
            node.retVal.accept(this);
            if (toConst.get(node.retVal) != null) 
                node.retVal = toConst.get(node.retVal);
        }
    }

    @Override public void visit(DefvarstmtNode node) {
        for (DefvarNode d : node.defVars)
            d.accept(this);
    }

    @Override public void visit(MemberexprNode node) {
        if (node.method != null) 
            node.method.accept(this);
    }

    @Override public void visit(ArrexprNode node) {
        node.offset.accept(this);
        if (toConst.get(node.offset) != null) 
            node.offset = toConst.get(node.offset);
    }

    @Override public void visit(FuncallexprNode node) {
        for (ExprNode e : node.args) {
            e.accept(this);
            if (toConst.get(e) != null)
                e = toConst.get(e);
        }
    }

    @Override public void visit(NewexprNode node) {
        for (ExprNode e : node.defDims) {
            e.accept(this);
            if (toConst.get(e) != null)
                e = toConst.get(e);
        }
    }

    @Override public void visit(UnaryexprNode node) {
        node.expr.accept(this);
        if (toConst.get(node.expr) != null) {
            node.expr = toConst.get(node.expr);
        }
    }

    @Override public void visit(BinaryexprNode node) {
        node.lhs.accept(this);
        if (toConst.get(node.lhs) != null) 
            node.lhs = toConst.get(node.lhs);
        node.rhs.accept(this);
        if (toConst.get(node.rhs) != null)
            node.rhs = toConst.get(node.rhs);
        if (node.lhs instanceof LiterexprNode && node.rhs instanceof LiterexprNode) {
            if (((LiterexprNode)node.lhs).typeName.equals("int") && ((LiterexprNode)node.rhs).typeName.equals("int")) {
                int lv = Integer.valueOf(((LiterexprNode)node.lhs).value);
                int rv = Integer.valueOf(((LiterexprNode)node.rhs).value);
                int res = 0;
                switch (node.op) {
                    case "+":
                        res = lv + rv;
                        break;
                    case "-":
                        res = lv - rv;
                        break;
                    case "*":
                        res = lv * rv;
                        break;
                    case "/":
                        res = lv / rv;
                        break;
                    case "%":
                        res = lv % rv;
                        break;
                    case ">>":
                        res = lv >> rv;
                        break;
                    case "<<":
                        res = lv << rv;
                        break;
                    case "&":
                        res = lv & rv;
                        break;
                    case "|":
                        res = lv | rv;
                        break;
                    case "^":
                        res = lv ^ rv;
                        break;
                    default:
                        break;
                }
                LiterexprNode resNode = new LiterexprNode("int", String.valueOf(res));
                toConst.put(node, resNode);
            }
        }
    }

    @Override public void visit(AssignexprNode node) {
        if (node.rhs != null) {
            node.rhs.accept(this);
            if (toConst.get(node.rhs) != null)
                node.rhs = toConst.get(node.rhs);
        }
    }

    @Override public void visit(IdexprNode node) {
        assert false;
    }

    @Override public void visit(LiterexprNode node) {
        assert false;
    }
}