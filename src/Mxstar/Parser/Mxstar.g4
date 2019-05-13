grammar Mxstar;

compilation_unit
	: top_defs* EOF
;

top_defs
	: defun
	| defclass
	| defvars
;

defvars
	: type vars ';'
;

vars
	: var (',' var)*
;

var
	: NAME ('=' expr)?
;

defun
	: type NAME '(' params? ')' block
;

defclass
	: CLASS NAME '{' (defun | defconstruct | defvars)* '}'
;

defconstruct
	: NAME '(' params? ')' block
;

block
	: '{' stmts '}'
;

params
	: param (',' param)*
;

param
	: type NAME
;

type
	: typeref_base ('[' empty ']')*
;

typeref_base
	: prim_type
	| class_type
;

empty : ;

prim_type
	: token=INT
	| token=BOOL
	| token=VOID
;

class_type
	: token=STRING
	| token=NAME
;

stmts
	: (stmt)*
;

stmt
	: ';'
	| expr_stmt
	| block_stmt
	| if_stmt
	| while_stmt
	| for_stmt
	| break_stmt
	| continue_stmt
	| return_stmt
	| defvar_stmt
;

expr_stmt
	: expr ';'
;

block_stmt
	: '{' stmts '}'
;

if_stmt
	: IF '(' expr ')' stmt (ELSE stmt)?
;

while_stmt
	: WHILE '(' expr ')' stmt
;

for_stmt
	: FOR '(' forInit=expr? ';' forCondition=expr? ';' forUpdate=expr? ')' stmt
;

break_stmt
	: BREAK ';'
;

continue_stmt
	: CONTINUE ';'
;

return_stmt
	: RETURN expr? ';'
;

defvar_stmt
	: defvars
;

expr
    :   '(' expr ')'								#   primaryExpr
    |   token=THIS 									#   primaryExpr
    |   token=INT_LITERAL							#   primaryExpr
    |   token=STRING_LITERAL 						#   primaryExpr
    |   token=BOOL_LITERAL							#   primaryExpr
    |   token=NULL_LITERAL							#   primaryExpr
    |   token=NAME									#   primaryExpr
    |   expr op='.' ( NAME | functionCall )   		#   memberExpr
    |   expr '[' expr ']'  		 					#   arrayExpre
    |   functionCall								#   funcCallExpr
    |   NEW creator									#   newExpr
    |   expr postfix=('++' | '--')					#   unaryExpr
    |   prefix=('+' | '-' | '++' | '--') expr 		#   unaryExpr
    |   prefix=('~' | '!') expr               		#   unaryExpr
    |   lhs=expr op=('*'|'/'|'%') rhs=expr         	#   binaryExpr
    |   lhs=expr op=('+'|'-') rhs=expr             	#   binaryExpr
    |   lhs=expr op=('<<'|'>>') rhs=expr           	#   binaryExpr
    |   lhs=expr op=('<='|'>='|'<'|'>') rhs=expr   	#   binaryExpr
    |   lhs=expr op=('=='|'!=') rhs=expr           	#   binaryExpr
    |   lhs=expr op='&' rhs=expr                   	#   binaryExpr
    |   lhs=expr op='^' rhs=expr                   	#   binaryExpr
    |   lhs=expr op='|' rhs=expr                   	#   binaryExpr
    |   lhs=expr op='&&' rhs=expr                  	#   binaryExpr
    |   lhs=expr op='||' rhs=expr                  	#   binaryExpr
    |   <assoc=right> lhs=expr op='=' rhs=expr     	#   assignExpr
    ;

creator
	: typeref_base (('[' expr ']')* ('[' empty ']')* | ('(' ')'))
;

functionCall
	: NAME '(' (expr (',' expr)*) ? ')'
;

BOOL_LITERAL : 'true' | 'false';
NULL_LITERAL : 'null';
STRING : 'string';
INT : 'int';
BOOL : 'bool';
VOID : 'void';
CLASS : 'class';
IF : 'if';
WHILE : 'while';
FOR : 'for';
BREAK : 'break';
CONTINUE : 'continue';
RETURN : 'return';
THIS : 'this';
NEW : 'new';
ELSE : 'else';


INT_LITERAL : [0-9][0-9]*;
STRING_LITERAL : '"' ('\\"' | '\\\\' | .)*?  '"';
NAME : [a-zA-Z][a-zA-Z0-9_]*;
LINE_COMMENT : '//'~[\n]*     ->  skip;
BLOCK_COMMENT : '/*' .*? '*/'   ->  skip;
WS : [ \t\r\n]+  ->  skip;