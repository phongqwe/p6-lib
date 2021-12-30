grammar Formula;

// a formula always start with '='
formula: '='expr
        |lit;

// an expression always returns something
expr: functionCall #funCall
    | '('expr')' #parenExpr
    | lit #literal
    | op=('-'|'+') expr #unExpr
    | expr op=('-'|'+'|'*'|'/'|'%') expr #binExpr
    | sheetRangeAddress #sheetRangeAddr
    ;

functionCall: functionName'('(expr)?(','expr)* ','?')';
functionName:ID(INT|ID)*;

sheetRangeAddress: SHEET_PREFIX?rangeAddress;

// 'Sheet123'!A1:A2, Sheet123!A1:A2
rangeAddress:cellAddress':'cellAddress
            | cellAddress
            | wholeColAddress
            | wholeRowAddress
            |'('rangeAddress')'
            ;

// A1,A123, ABC123
cellAddress: ID INT;

// A:A, A:B
wholeColAddress: ID ':' ID;

//1:1, 1:123
wholeRowAddress: INT':'INT;

// literal
lit: (FLOAT_NUMBER | STRING | INT);

//FUNCTION_NAME:ID(INT|ID)*;
SHEET_PREFIX:'\'' ID(' '|INT|ID)*? '\''   '!'
            | ID(' '|INT|ID)* '!';
ID:ID_LETTER(ID_LETTER)*;
fragment ID_LETTER:'a'..'z'|'A'..'Z'|'_';

// number
FLOAT_NUMBER: DIGIT+ '.' DIGIT*
        |'.' DIGIT+
        ;

INT:DIGIT+;
fragment DIGIT:[0-9] ;
// string
STRING: '"' (ESC|.)*? '"' ;// match anything in "..."
fragment ESC : '\\"' | '\\\\' ; // 2-char sequences \" and \\

// operator
MUL: '*';
DIV: '/';
ADD: '+';
SUB: '-';
MOD: '%'; //modulo
EXP: '^'; //exponential

NEWLINE:'\r'? '\n';
WS: [ \t]+ -> skip;
