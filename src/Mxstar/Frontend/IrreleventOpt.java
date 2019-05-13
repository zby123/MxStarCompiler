package Mxstar.Frontend;

import Mxstar.AST.*;
import Mxstar.ST.ArrayType;
import Mxstar.ST.VariableSymbol;

import java.util.*;

public class IrreleventOpt implements ASTVisitor {
    private ASTProgram astProg;
    private HashSet<VariableSymbol> ReleSet;
    private HashMap<ASTNode,HashSet<VariableSymbol>> usedMap;
    private HashMap<ASTNode,HashSet<VariableSymbol>> definedMap;

    public void run() {
        astProg.accept(this);
    }

    @Override
    public void visit(ASTProgram node) {
        usedMap.clear();
        definedMap.clear();
        ReleSet.clear();
        ReleSet.add(fCIndicator);
        ReleSet.add(gVIndicator);
        ReleSet.add(iIndicator);

        getSymbol = true;
        for(DefunNode fd : node.functions)
            fd.accept(this);
        getSymbol = false;
        calcRele = true;
        int lastSize = -1;
        while(lastSize != ReleSet.size()) {
            lastSize = ReleSet.size();
            for(DefunNode fd : node.functions)
                fd.accept(this);
        }
        calcRele = false;

        for(DefunNode fd : node.functions)
            fd.accept(this);
    }

    @Override
    public void visit(DefNode node) { }

    @Override
    public void visit(DefclassNode node) { }

    @Override
    public void visit(DefvarNode node) {
        if(getSymbol) {
            initSet(node);
            if (node.init != null) {
                node.init.accept(this);
                addRele(node, node.init);
            }
            definedMap.get(node).add(node.symbol);
        } else if(calcRele) {
            setRele(node, node.init);
            if(node.init != null)
                node.init.accept(this);
        }

    }

    private void work(List<StmtNode> list) {
        HashSet<StmtNode> needToRemove = new HashSet<>();
        for (StmtNode statement : list) {
            if (removable(statement))
                needToRemove.add(statement);
            else
                statement.accept(this);
        }
        list.removeAll(needToRemove);
    }

    @Override
    public void visit(DefunNode node) {
        if(getSymbol) {
            for (StmtNode statement : node.funcBody)
                statement.accept(this);
        } else if(calcRele) {
            for(StmtNode statement : node.funcBody)
                statement.accept(this);
        } else {
            work(node.funcBody);
        }
    }

    private void initSet(ASTNode a) {
        definedMap.put(a, new HashSet<>());
        usedMap.put(a, new HashSet<>());
    }

    @Override
    public void visit(TypeNode node) { }

    @Override
    public void visit(ArrTypeNode node) { }

    @Override
    public void visit(PrimTypeNode node) { }

    @Override
    public void visit(ClassTypeNode node) { }

    @Override
    public void visit(StmtNode node) { }

    private boolean removable(ASTNode node) {
        HashSet<VariableSymbol> defined = new HashSet<>(definedMap.get(node));
        defined.retainAll(ReleSet);
        return defined.isEmpty();
    }

    private void addRele(ASTNode a, ASTNode b) {
        definedMap.get(a).addAll(definedMap.get(b));
        usedMap.get(a).addAll(usedMap.get(b));
    }

    private void setRele(ASTNode a, ASTNode... reqs) {
        setRele(a, Arrays.asList(reqs));
    }
    private void setRele(ASTNode a, List<? extends ASTNode> reqs) {
        if(!removable(a)) {
            for(ASTNode req : reqs) {
                if(req == null) continue;
                ReleSet.addAll(usedMap.get(req));
            }
        }
    }

    @Override
    public void visit(ForstmtNode node) {
        if(getSymbol) {
            initSet(node);
            if(node.initCond != null) {
                node.initCond.accept(this);
                addRele(node, node.initCond);
            }
            if(node.endCond != null) {
                node.endCond.accept(this);
                addRele(node, node.endCond);
            }
            if(node.upd != null) {
                node.upd.accept(this);
                addRele(node, node.upd);
            }
            if (node.body != null) {
                node.body.accept(this);
                addRele(node, node.body);
            }
        } else if(calcRele) {
            setRele(node, node.initCond, node.endCond, node.upd);
            if (node.body != null) {
                node.body.accept(this);
            }
        } else {
            if(node.body != null && removable(node.body)) {
                node.body = null;
            } else {
                if (node.body != null) {
                    node.body.accept(this);
                }
            }
        }
    }

    @Override
    public void visit(WhilestmtNode node) {
        if(getSymbol) {
            initSet(node);
            node.condition.accept(this);
            addRele(node, node.condition);
            node.body.accept(this);
            addRele(node, node.body);
        } else if(calcRele) {
            setRele(node, node.condition);
            node.body.accept(this);
        } else {
            node.body.accept(this);
        }
    }

    @Override
    public void visit(IfstmtNode node) {
        if(getSymbol) {
            initSet(node);
            node.condition.accept(this);
            addRele(node, node.condition);
            node.body.accept(this);
            addRele(node, node.body);
            if (node.elseBody != null) {
                node.elseBody.accept(this);
                addRele(node, node.elseBody);
            }
        } else if(calcRele) {
            setRele(node, node.condition);
            node.condition.accept(this);
            node.body.accept(this);
            if(node.elseBody != null) node.elseBody.accept(this);
        } else {
            node.body.accept(this);
            if(node.elseBody != null)
                node.elseBody.accept(this);
        }
    }

    @Override
    public void visit(ContinuestmtNode node) {
        if(getSymbol) {
            initSet(node);
            definedMap.get(node).add(iIndicator);
        }
    }

    @Override
    public void visit(BreakstmtNode node) {
        if(getSymbol) {
            initSet(node);
            definedMap.get(node).add(iIndicator);
        }
    }

    @Override
    public void visit(ReturnstmtNode node) {
        if(getSymbol) {
            initSet(node);
            if(node.retVal != null) {
                node.retVal.accept(this);
                addRele(node, node.retVal);
                ReleSet.addAll(usedMap.get(node.retVal));
            }
            definedMap.get(node).add(gVIndicator);
        } else if(calcRele) {
            if(node.retVal != null)
                node.retVal.accept(this);
        }
    }

    @Override
    public void visit(BlockstmtNode node) {
        if(getSymbol) {
            initSet(node);
            for (StmtNode statement : node.stmts) {
                statement.accept(this);
                addRele(node, statement);
            }
        } else if(calcRele) {
            for(StmtNode s : node.stmts)
                s.accept(this);
        } else {
            work(node.stmts);
        }
    }

    @Override
    public void visit(DefvarstmtNode node) {
        if(getSymbol) {
            initSet(node);
            for (DefvarNode d : node.defVars){
                d.accept(this);
                addRele(node, d);
            }
        } else if(calcRele) {
            for (DefvarNode d : node.defVars){
                setRele(node, d);
                d.accept(this);
            }
        }
    }

    @Override
    public void visit(ExprstmtNode node) {
        if(getSymbol) {
            initSet(node);
            node.expr.accept(this);
            addRele(node, node.expr);
        } else if(calcRele) {
            setRele(node, node.expr);
            node.expr.accept(this);
        }
    }

    @Override
    public void visit(ExprNode node) { }

    @Override
    public void visit(IdexprNode node) {
        if(getSymbol) {
            initSet(node);
            if(node.symbol.isGlobalVariable) {
                definedMap.get(node).add(gVIndicator);
            } else if(inAssign.getLast()) {
                definedMap.get(node).add(node.symbol);
                usedMap.get(node).add(node.symbol);
            } else {
                usedMap.get(node).add(node.symbol);
            }
        }
    }

    @Override
    public void visit(LiterexprNode node) {
        if(getSymbol) {
            initSet(node);
        }
    }

    @Override
    public void visit(ArrexprNode node) {
        if(getSymbol) {
            initSet(node);
            node.addr.accept(this);
            addRele(node, node.addr);
            inAssign.addLast(false);
            node.offset.accept(this);
            inAssign.removeLast();
            addRele(node, node.offset);
        }
    }

    @Override
    public void visit(FuncallexprNode node) {
        if(getSymbol) {
            initSet(node);
            for(ExprNode expression : node.args) {
                expression.accept(this);
                addRele(node, expression);
            }
            if (node.functionSymbol != null && node.functionSymbol.isImportantFunc) {
                definedMap.get(node).add(fCIndicator);
                ReleSet.addAll(usedMap.get(node));
            }
        } else if(calcRele) {
            setRele(node, node.args);
            for(ExprNode expression : node.args)
                expression.accept(this);
        }
    }

    @Override
    public void visit(NewexprNode node) {
        if(getSymbol) {
            initSet(node);
            for(ExprNode expression : node.defDims) {
                expression.accept(this);
                addRele(node, expression);
            }
        } else if(calcRele) {
            setRele(node, node.defDims);
            for(ExprNode expression : node.defDims) {
                expression.accept(this);
            }
        }
    }

    @Override
    public void visit(MemberexprNode node) {
        if(getSymbol) {
            initSet(node);
            node.obj.accept(this);
            addRele(node, node.obj);
            if(node.method != null) {
                node.method.accept(this);
                addRele(node, node.method);
            } else {
                node.member.accept(this);
                addRele(node, node.member);
            }
        } else {
            setRele(node, node.obj, node.method, node.member);
            node.obj.accept(this);
            if(node.method != null)
                node.method.accept(this);
            else
                node.member.accept(this);
        }
    }

    @Override
    public void visit(UnaryexprNode node) {
        if(getSymbol) {
            initSet(node);
            node.expr.accept(this);
            addRele(node, node.expr);
            if(node.op.contains("++") || node.op.contains("--"))
                definedMap.get(node).addAll(usedMap.get(node.expr));
        } else if(calcRele){
            setRele(node, node.expr);
            node.expr.accept(this);
        }
    }

    @Override
    public void visit(BinaryexprNode node) {
        if(getSymbol) {
            initSet(node);
            node.lhs.accept(this);
            addRele(node, node.lhs);
            node.rhs.accept(this);
            addRele(node, node.rhs);
        } else if(calcRele){
            setRele(node, node.lhs, node.rhs);
            node.lhs.accept(this);
            node.rhs.accept(this);
        }
    }

    @Override
    public void visit(AssignexprNode node) {
        if(getSymbol) {
            initSet(node);
            inAssign.addLast(true);
            node.lhs.accept(this);
            inAssign.removeLast();
            addRele(node, node.lhs);
            definedMap.get(node).addAll(definedMap.get(node.lhs));
            node.rhs.accept(this);
            addRele(node, node.rhs);
            if(node.rhs.type instanceof ArrayType)
                definedMap.get(node).addAll(usedMap.get(node.rhs));
        } else if(calcRele) {
            setRele(node, node.lhs, node.rhs);
            node.lhs.accept(this);
            node.rhs.accept(this);
        }
    }
    private boolean getSymbol;
    private boolean calcRele;
    private VariableSymbol fCIndicator;
    private VariableSymbol gVIndicator;
    private VariableSymbol iIndicator;
    private LinkedList<Boolean> inAssign;

    public IrreleventOpt(ASTProgram astProg) {
        this.astProg = astProg;
        this.usedMap = new HashMap<>();
        this.definedMap = new HashMap<>();
        this.ReleSet = new HashSet<>();
        this.fCIndicator = new VariableSymbol(null, null, null,false, false);
        this.gVIndicator = new VariableSymbol(null, null, null, false, false);
        this.iIndicator = new VariableSymbol(null, null, null, false, false);
        this.inAssign = new LinkedList<>();
        this.inAssign.addLast(false);
    }
}
