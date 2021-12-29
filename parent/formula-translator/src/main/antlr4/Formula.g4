grammar Formula;

formula: '='expr
        |lit;

expr: functionCall #funCall
    | '('expr')' #parenExpr
    | lit #literal
    | op=('-'|'+') expr #unExpr
    | expr op=('-'|'+'|'*'|'/'|'%') expr #binExpr
    | sheetRangeAddress #sheetRangeAddr
    ;

functionCall: functionName'('(functionArgs)?(','functionArgs)* ','?')';
functionArgs:expr|rangeAddress|sheetRangeAddress;
functionName:ID INT* ID*;

rangeAddress:cellAddress':'cellAddress
            | cellAddress
            | wholeColAddress
            | wholeRowAddress
            |'('rangeAddress')'
            ;
// 'Sheet123'!A1:A2, Sheet123!A1:A2
sheetRangeAddress: sheetPrefix?rangeAddress;
sheetPrefix:('\''ID' '* INT*' '* '\''|ID INT*) '!';

// A1,A123, ABC123
cellAddress: ID INT;

// A:A, A:B
wholeColAddress: ID':'ID;

//1:1, 1:123
wholeRowAddress: INT':'INT;

// literal
lit: (FLOAT_NUMBER | STRING|INT);

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
