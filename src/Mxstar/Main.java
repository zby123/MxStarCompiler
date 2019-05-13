package Mxstar;


import Mxstar.Parser.*;


import Mxstar.AST.ASTProgram;
import Mxstar.IR.IRProgram;
import Mxstar.IR.RegisterSet;
import Mxstar.Parser.MxstarLexer;
import Mxstar.Parser.MxstarParser;
import Mxstar.ST.GlobalScopeTree;
import Mxstar.Exceptions.*;
import Mxstar.Frontend.*;
import Mxstar.Backend.*;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.*;

import java.io.*;
import java.util.*;
import static java.lang.System.exit;

public class Main {
	private static ParseTree tree;

	private static String getFileInput(String filePath) {
		String ret = new String();
		File file = new File(filePath);
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(file));
			String tempString = null;
			while ((tempString = reader.readLine()) != null) {
				//ErrorReporter.getInstance().putLine(tempString);
				ret += tempString + '\n';
			}
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e1) {
				}
			}
		}
		return ret;
	}

	public static void compile(String prog) {
		Vector<String> errors = new Vector<>();
		CharStream input = CharStreams.fromString(prog);
		MxstarLexer mstarLexer = new MxstarLexer(input);
		CommonTokenStream tokens = new CommonTokenStream(mstarLexer);
		MxstarParser parser = new MxstarParser(tokens);

		ErrorRecorder errorRecorder = new ErrorRecorder();

		parser.removeErrorListeners();
		SyntaxErrorListener syntaxErrorListener = new SyntaxErrorListener(errorRecorder);
		parser.addErrorListener(syntaxErrorListener);

		ParseTree parseTree = parser.compilation_unit();
		if(errorRecorder.errorOccured()) {
        	errorRecorder.printTo(System.err);
			exit(1);
		}
		
		ASTBuilder astBuilder = new ASTBuilder(errorRecorder);
		astBuilder.visit(parseTree);
		if(errorRecorder.errorOccured()) {
        	errorRecorder.printTo(System.err);
			exit(1);
		}

		ASTProgram astProg = astBuilder.getAstProg();
		STBuilder stBuilder = new STBuilder(errorRecorder);
		astProg.accept(stBuilder);
		if(errorRecorder.errorOccured()) {
        	errorRecorder.printTo(System.err);
			exit(1);
		}
		
		GlobalScopeTree globalST = stBuilder.globalST;
        SemanticChecker semanticChecker = new SemanticChecker(globalST, errorRecorder);
        astProg.accept(semanticChecker);
        if(errorRecorder.errorOccured()) {
        	errorRecorder.printTo(System.err);
            exit(1);
        }
        RegisterSet.init();

        ConstantFolder cf = new ConstantFolder();
        astProg.accept(cf);

        IrreleventOpt Iro = new IrreleventOpt(astProg);
        Iro.run();
        
        IRbuilder irBuilder = new IRbuilder(globalST);
        astProg.accept(irBuilder);
        IRProgram irProg = irBuilder.irProgram;
        Fixer fxr = new Fixer();
        irProg.accept(fxr);

		RegAllocator allocator = new RegAllocator(irProg);
		allocator.run();
		SFBuilder sfbuilder = new SFBuilder(irProg);
        sfbuilder.run();

        IRPrinter irPrinter = new IRPrinter();
        irPrinter.visit(irProg);
        irPrinter.printTo(System.out);
	}
	
	public static void main(String[] args) {
		String program;
		if (args.length > 0) program = getFileInput(args[0]);
		else program = getFileInput("program.cpp");
		compile(program);

	}
}