This is an interpreter for the Java implementation of the Focaccia language (Fojaccia), by way of Robert Nystrom's Crafting Interpreters.

### Installation
Ensure you have Java 21 installed and build Fojaccia.java with your favorite IDE. I don't have Maven goals configured yet, but eventually this will be less hand-wavey. 


## Language Crap For Nerds

### Grammar
```
program         -> declaration* EOF ;
declaration     -> fnDec | varDec | statement ;
fnDec           -> "fn" function ;
function        -> IDENTIFIER "(" parameters? ")" block ;
parameters      -> IDENTIFIER ( "," IDENTIFIER )* ;
varDec          -> "var" IDENTIFIER ( "=" statement )? ";" ;
statement       -> exprStmt | forStmt | ifStmt | printStmt | whileStmt | block ;
exprStmt        -> expression ";" ;
forStmt         -> "for" "(" ( varDec | exprStmt | ";" ) expression? ";" expression ")" statement ;
ifStmt          -> "if" "(" expression ")" statement ( "else" statement )? ;
printStmt       -> "print" expression ";" ;
whileStmt       -> "while" "(" expression ")" statement ;
block           -> "{" declaration "}" ;
expression      -> literal
                   | unary
                   | binary
                   | grouping 
                   | call ;
literal         -> NUMBER | STRING | "true" | "false" | "null" ;
unary           -> ( "-" | "!" ) expression ;
binary          -> expression operator expression ;
grouping        -> "(" expression ")" ;
call            -> primary ( "(" arguments? ")" )* ;
arguments       -> expression ( "," expression )* ;
operator        ->   "=="  | "!=" | "<" | "<=" | ">" 
                   | ">="  | "+"  | "-" | "*"  | "/"
                   | "and" | "or" |"var" ;
```
### Precedence
```
declaration     -> statement
statement       -> expression
expression      -> assignment ;
assignment      -> IDENTIFIER "=" assignment | equality | logic_or ;
logic_or        -> logic_and ( "or" logic_and )* ;
logic_and       -> equality ( "and" equality )* ;
equality        -> comparison ( ( "==" | "!=" ) comparison )* ;
comparison      -> term ( ( ">" | ">=" | "<" | "<=" ) term )* ;
term            -> factor ( ( "-" | "+" ) factor )* ;
factor          -> unary ( ( "/" | "*" ) unary )* ;
unary           -> ( "!" | "-" ) unary 
                   | primary ;
primary         -> NUMBER | STRING | "true" | "false" 
                   | "null"
                   | "(" expression ")" 
                   | IDENTIFIER ;
```