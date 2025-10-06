This is an interpreter for the Java implementation of the Focaccia language (Fojaccia), by way of Robert Nystrom's Crafting Interpreters.

### Installation
Ensure you have Java 21 installed and build Fojaccia.java with your favorite IDE. I don't have Maven goals configured yet, but eventually this will be less hand-wavey. 


## Language Crap For Nerds

### Basic Grammar
```
program         -> declaration* EOF ;
block           -> "{" declaration "}" ;
declaration     -> varDec | statement ;
statement       -> exprStmt | printStmt | block ;
varDec          -> "var" IDENTIFIER ( "=" statement )? ";" ;
exprStmt        -> expression ";" ;
printStmt       -> "print" expression ";" ;
expression      -> literal
                   | unary
                   | binary
                   | grouping ;
literal         -> NUMBER | STRING | "true" | "false" | "null" ;
unary           -> ( "-" | "!" ) expression ;
binary          -> expression operator expression ;
grouping        -> "(" expression ")" ;
operator        ->   "=="  | "!=" | "<" | "<=" | ">" 
                   | ">="  | "+"  | "-" | "*"  | "/"
                   | "and" | "or" |"var" ;
```
### Precedence
```
declaration     -> statement
statement       -> expression
expression      -> assignment ;
assignment      -> IDENTIFIER "=" assignment | equality ;
equality        -> comparison ( ( "==" | "!=" ) comparison )* ;
comparison      -> term ( ( ">" | ">=" | "<" | "<=" ) term )* ;
term            -> factor ( ( "-" | "+" ) factor )* ;
factor          -> unary ( ( "/" | "*" ) unary )* ;
unary           -> ( "!" | "-" ) unary 
                   | primary ;
primary         -> ;NUMBER | STRING | "true" | "false" 
                   | "null"
                   | "(" expression ")" 
                   | IDENTIFIER ;
```