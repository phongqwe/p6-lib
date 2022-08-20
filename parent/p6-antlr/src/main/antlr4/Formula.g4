grammar Formula;

// a formula always start with '='
formula: '='expr EOF #zFormula
        ;

// an expression always returns something
expr: functionCall #funCall
    | '('expr')' #parenExpr
    | lit #literal
    | op=SUB expr #unSubExpr
    | op=NOT expr #notExpr
    | expr op=EXP expr # powExpr
    | expr op=(MUL|DIV|MOD) expr #mulDivModExpr
    | expr op=(ADD|SUB) expr #addSubExpr
    | expr op=(AND|OR) expr # andOrExpr
    | SHEET_PREFIX?rangeAddress #sheetRangeAddrExpr //Sheet1!C1, 'Sheet 123'!C1
    ;

functionCall: functionName'('(expr)?(','expr)* ','?')';

rangeAddress:cellAddress':'cellAddress  #rangeAsPairCellAddress
            | cellAddress  #rangeAsOneCellAddress
            | ID ':' ID  #rangeAsColAddress
            | INT':'INT #rangeAsRowAddress
            |'('rangeAddress')' #rangeInparens
            ;

// A1,A123, ABC123, $A1, A$1, $A$1
cellAddress: '$'?ID '$'?INT;

// literal
BOOLEAN: 'TRUE' | 'FALSE';
lit: (FLOAT_NUMBER | BOOLEAN | STRING | INT );

// sheet prefix may or may not encased in single quote, ends with "!". Eg: 'My Sheet'!, MySheet!
SHEET_PREFIX:'\'' ID(' '|INT|ID)*? '\''   '!'
            | ID(INT|ID)* '!';
ID:ID_LETTER(ID_LETTER)*;
fragment ID_LETTER:'a'..'z'|'A'..'Z'|'_';

FLOAT_NUMBER: DIGIT+ '.' DIGIT*
        |'.' DIGIT+
        ;

INT:DIGIT+;
fragment DIGIT:[0-9] ;

// string
STRING: '"' (ESC|.)*? '"' ;// match anything in "..."
fragment ESC : '\\"' | '\\\\' ; // 2-char sequences \" and \\
functionName:ID(INT|ID)* ;
// operator
MUL: '*';
DIV: '/';
ADD: '+';
SUB: '-';
MOD: '%'; //modulo
EXP: '^'; //exponential
// boolean operators
AND: '&&';
OR: '||';
NOT:'!';

NEWLINE:'\r'? '\n';
WS: [ \t]+ -> skip;
