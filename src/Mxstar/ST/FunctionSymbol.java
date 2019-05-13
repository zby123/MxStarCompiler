package Mxstar.ST;

import Mxstar.AST.TokenLoc;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

public class FunctionSymbol {
    public String name;
    public TokenLoc loc;
    public VariableType returnType;
    public List<VariableType> parameterTypes;
    public List<String> parameterNames;
    public ScopeTree functionScopeTree;
    public HashSet<VariableSymbol> GV;
    public boolean isGlobalFunction;
    public boolean isImportantFunc;
    public HashSet<FunctionSymbol> calleeSet;

    private HashSet<FunctionSymbol> visited;

    public FunctionSymbol() {
        this.parameterTypes = new LinkedList<>();
        this.parameterNames = new LinkedList<>();
        this.GV = new HashSet<>();
        this.calleeSet = new HashSet<>();
        this.visited = new HashSet<>();
    }

    private void dfsSideEffect(FunctionSymbol fs) {
        if(isImportantFunc) return;
        if(visited.contains(fs)) return;
        visited.add(fs);
        for(FunctionSymbol sfs : fs.calleeSet) {
            if(sfs.isImportantFunc) {
                isImportantFunc = true;
                break;
            }
        }
    }

    private boolean isPrimitiveType(VariableType vt) {
        return vt instanceof PrimitiveType;
    }

    public void finish() {
        for(VariableType vt : parameterTypes) {
            if(!isPrimitiveType(vt))
                isImportantFunc = true;
        }
        visited.clear();
        dfsSideEffect(this);
    }
}
