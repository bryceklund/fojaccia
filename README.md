Interpreter for the Java implementation of the Focaccia language (Fojaccia), by way of Robert Nystrom's Crafting Interpreters

### Basic Grammar
```
expression      -> literal
                   | unary
                   | binary
                   | grouping ;
 
literal         -> NUMBER | STRING | "true" | "false" | "null" ;
unary           -> ( "-" | "!" ) expression ;
binary          -> expression operator expression ;
grouping        -> "(" expression ")" ;
operator        ->   "=="  | "!=" | "<" | "<=" | ">" 
                   | ">="  | "+"  | "-" | "*"  | "/" ;
```
### Precedence
```
expression      -> equality
equality        -> comparison ( ( "==" | "!=" ) comparison )* ;
comparison      -> term ( ( ">" | ">=" | "<" | "<=" ) term )* ;
term            -> factor ( ( "-" | "+" ) factor )* ;
factor          -> unary ( ( "/" | "*" ) unary )* ;
unary           -> ( "!" | "-" ) unary 
                   | primary ;
primary         -> NUMBER | STRING | "true" | "false" | "null"
                   | "(" expression ")" ;
```