# Mx* Compiler 中期报告



## Implementation

本项目是一个简易的Mx*语言的编译器，使用Java实现。目前实现了Lexing, Parsing, Semantic Check三个阶段。Lexer 与 Parser 由 ANTLR4 生成。

在使用 Lexer 与 Parser 构建出 ParseTree 后，使用 ANTLR 提供的 Visitor 方法构建出抽象表达式树。在建立好AST后，调用两次 Visitor 分别构建关于变量作用域的 Symbol Table 以及对构建好的 AST 和 Symbol Table 进行Semantic Check，以判断代码是否符合语法规范和没有语法错误。



## Features

#### 完善且用户友好的错误显示

如果发生编译错误，本项目能够友好地输出错误的种类，发生的位置和阶段，比如：

```
SyntaxError:(11,1):missing '{' at 'return'
```

或者

```
SemanticError:(1,16):type conflict with defined variable
```

