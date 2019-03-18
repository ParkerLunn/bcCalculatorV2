grammar Calculator;

parse
 : block EOF
 ;

block
 : stat*
 ;

stat
 : assignment
 | if_stat 
 | while_stat
 | for_stat
 | cont
 | brk 
 | ret
 | print
 | OTHER {System.err.println("unknown char: " + $OTHER.text);}
 ;

assignment
 : ID ASSIGN expr 
 ;

if_stat
 : IF condition_block (ELSE IF condition_block)* (ELSE stat_block)?
 ;

condition_block
 : expr stat_block
 ;

stat_block
 : OBRACE block CBRACE
 | stat
 ;

while_stat
 : WHILE expr stat_block
 ;

for_stat
 : FOR OPAR assignment SCOL expr SCOL expr CPAR stat_block
 ;

print
 : expr
 | PRINT (expr',')* expr
 ;

cont: CONT ;
brk: BREAK ;
ret: RET ;

expr
 : expr POW<assoc=right> expr           #powExpr
 | op=(SIN | COS | LOG | EXP | SQRT | READ) OPAR expr CPAR #libFuncExpr
 | INC ID                               #preIncExpr
 | ID INC                               #postIncExpr
 | DEC ID                               #preDecExpr
 | ID DEC                               #postDecExpr
 | MINUS expr                           #unaryMinusExpr
 | NOT expr                             #notExpr
 | expr op=(MULT | DIV | MOD) expr      #multiplicationExpr
 | expr op=(PLUS | MINUS) expr          #additiveExpr
 | expr op=(LTEQ | GTEQ | LT | GT) expr #relationalExpr
 | expr op=(EQ | NEQ) expr              #equalityExpr
 | expr AND expr                        #andExpr
 | expr OR expr                         #orExpr
 | atom                                 #atomExpr
 ;


atom
 : OPAR expr CPAR #parExpr
 | (INT | FLOAT)  #numberAtom
 | (TRUE | FALSE) #booleanAtom
 | ID             #idAtom
 | STRING         #stringAtom
 | NIL            #nilAtom
 ;

INC: '++';
DEC: '--';
OR : '||';
AND : '&&';
EQ : '==';
NEQ : '!=';
GT : '>';
LT : '<';
GTEQ : '>=';
LTEQ : '<=';
PLUS : '+';
MINUS : '-';
MULT : '*';
DIV : '/';
MOD : '%';
POW : '^';
NOT : '!';

SCOL : ';';
ASSIGN : '=';
OPAR : '(';
CPAR : ')';
OBRACE : '{';
CBRACE : '}';

TRUE : 'true';
FALSE : 'false';
NIL : 'nil';
IF : 'if';
ELSE : 'else';
WHILE : 'while';
FOR: 'for';
PRINT : 'print';
BREAK : 'break';
CONT: 'continue';
RET: 'return'; 

SIN: 's';
COS: 'c';
LOG: 'l';
EXP: 'e';
SQRT: 'sqrt';

READ: 'read';

ID
 : [a-zA-Z_] [a-zA-Z_0-9]*
 ;

INT
 : [0-9]+
 ;

FLOAT
 : [0-9]+ '.' [0-9]* 
 | '.' [0-9]+
 ;

STRING
 : '"' (~["\r\n] | '""')* '"'
 ;
COMMENT
 : '#' ~[\r\n]* -> skip
 ;
SPACE
 : [ \t\r\n] -> skip
 ;
OTHER
 : . 
 ;