This interpreter is the Java implementation of the Focaccia programming language (Fojaccia), by way of Robert Nystrom's Crafting Interpreters. This was created solely as an educational exercise. It is written in Java, parses with an AST, and has a relatively limited feature set. It's a silly language and it has a silly name. 

### Installation
- Ensure you have Java 21 installed
- Run the REPL:
```mvn exec:java -Dexec.mainClass="fojaccia.Fojaccia"```
- Run a script:
```mvn exec:java -Dexec.mainClass="fojaccia.Fojaccia" -Dexec.args="<path/to/file.foj>"```

### Examples
Your first Fojaccia program:
```
print "hello world";
```

Variable assignment:
```
var cat = "Rhubarb";
```

Function delcaration:
```
fn printCatName(name) {
  print name;
}
```

Classes and inheritance:
```
> class Doughnut { cook() { print "Fry until golden brown"; } }
> class BostonCream < Doughnut {}
> BostonCream().cook();
Fry until golden brown
> 
```
### Language Crap For Nerds

#### Grammar
```
program         -> declaration* EOF ;
declaration     -> classDec | fnDec | varDec | statement ;
classDec        -> "class" IDENTIFIER ( "<" IDENTIFIER )? "{" function* "}" ;
fnDec           -> "fn" function ;
function        -> IDENTIFIER "(" parameters? ")" block ;
parameters      -> IDENTIFIER ( "," IDENTIFIER )* ;
varDec          -> "var" IDENTIFIER ( "=" statement )? ";" ;
statement       -> exprStmt | forStmt | ifStmt | printStmt | whileStmt | block ;
returnStmt      -> "return" expression? ";" ;
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
call            -> primary ( "(" arguments? ")" | "." IDENTIFIER )* ;
arguments       -> expression ( "," expression )* ;
operator        ->   "=="  | "!=" | "<" | "<=" | ">" 
                   | ">="  | "+"  | "-" | "*"  | "/"
                   | "and" | "or" |"var" ;
```
#### Precedence
```
declaration     -> statement
statement       -> expression
expression      -> assignment ;
assignment      -> ( call "." )? IDENTIFIER "=" assignment | equality | logic_or ;
logic_or        -> logic_and ( "or" logic_and )* ;
logic_and       -> equality ( "and" equality )* ;
equality        -> comparison ( ( "==" | "!=" ) comparison )* ;
comparison      -> term ( ( ">" | ">=" | "<" | "<=" ) term )* ;
term            -> factor ( ( "-" | "+" ) factor )* ;
factor          -> unary ( ( "/" | "*" ) unary )* ;
unary           -> ( "!" | "-" ) unary 
                   | primary ;
primary         -> "true" | "false" | "null" | "this" 
                   | NUMBER | STRING | IDENTIFIER
                   | "(" expression ")" 
                   | "super" "." IDENTIFIER ;
```
