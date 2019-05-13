package Mxstar.Exceptions;

import Mxstar.AST.TokenLoc;

import java.io.PrintStream;
import java.util.LinkedList;
import java.util.List;

public class ErrorRecorder {
    private List<String> errorList;

    public ErrorRecorder() {
        errorList = new LinkedList<String>();
    }

    public void addRecord(TokenLoc location, String message) {
        StackTraceElement[] stacks = new Throwable().getStackTrace();
        if (stacks[1].getClassName().equals("Mxstar.Exceptions.SyntaxErrorListener"))
            errorList.add("SyntaxError:" + location + ":" + message);
        else 
            errorList.add("SemanticError:" + location + ":" + message);
    }
    public List<String> getErrorList() {
        return errorList;
    }
    public boolean errorOccured() {
        return !errorList.isEmpty();
    }

    public void printTo(PrintStream out) {
        out.print(toString());
    }

    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        for(String s : errorList) {
            stringBuilder.append(s + '\n');
        }
        return stringBuilder.toString();
    }

}
