grammar Formula;

// a formula always start with '='
formula: startFormulaSymbol expr EOF #zFormula
        ;

// an expression always returns something
expr: functionCall #funCall
    | openParen expr closeParen #parenExpr
    | lit #literal
    | op=SUB expr #unSubExpr
    | op=NOT expr #notExpr
    | expr op=EXP expr # powExpr
    | expr op=(MUL|DIV|MOD) expr #mulDivModExpr
    | expr op=(ADD|SUB) expr #addSubExpr
    | expr op=(AND|OR) expr # andOrExpr
    | expr op=(EQUAL|NOT_EQUAL|LARGER|LARGER_OR_EQUAL|SMALLER|SMALLER_OR_EQUAL) expr #boolOperation
    | rangeAddress sheetPrefix? wbPrefix? #fullRangeAddressExpr
    ;

functionCall: functionName openParen (expr)?(comma expr)* comma? closeParen;

rangeAddress:cellAddress ':' cellAddress  #rangeAsPairCellAddress
            | cellAddress  #rangeAsOneCellAddress
            | ID ':' ID  #rangeAsColAddress
            | INT':'INT #rangeAsRowAddress
            |openParen rangeAddress closeParen #rangeInparens
            ;

// A1,A123, ABC123, $A1, A$1, $A$1
cellAddress: '$'?ID '$'?INT;

// literal
BOOLEAN: 'TRUE' | 'FALSE';
lit: (FLOAT_NUMBER | BOOLEAN | STRING | INT );
sheetPrefix:'@'  sheetNameWithSpace
            |'@' sheetName;

sheetNameWithSpace:withSpaceId;
sheetName:noSpaceId;

wbPrefix: wbPrefixNoPath | wbPrefixWithPath;
wbPrefixNoPath:'@' wbName;
wbPrefixWithPath:'@' wbName '@' wbPath ;

wbName:wbNameNoSpace |  wbNameWithSpace ;
wbNameNoSpace:noSpaceId;
wbNameWithSpace:withSpaceId;

// wbPath is encased in single quotes: 'path/to/wb.abc'
//wbPath:'\'' (.)*? '\'';
wbPath:SINGLE_QUOTE_STRING;

noSpaceId:ID(INT|ID)*;
withSpaceId:SINGLE_QUOTE_STRING;
openParen:'(';
closeParen:')';
comma:',';
startFormulaSymbol:'=';
// | ',' | '='
// sheet prefix may or may not encased in single quote, ends with "!". Eg: 'My Sheet'!, MySheet!

ID:ID_LETTER(ID_LETTER)*;
fragment ID_LETTER:'a'..'z'|'A'..'Z'|'_';

FLOAT_NUMBER: DIGIT+ '.' DIGIT*
        |'.' DIGIT+
        ;

INT:DIGIT+;
fragment DIGIT:[0-9] ;

// string
STRING: '"' (ESC_CHAR|.)*? '"' ;// match anything in "..."
SINGLE_QUOTE_STRING:'\'' (ESC_CHAR|.)*? '\'';
//STRING: '"' (STRING_CONTENT)*? '"' ;// match anything in "..."
//STRING_CONTENT:(ESC_CHAR|.);
//LATIN_EXTENDED_A: '\u0100' .. '\u017E';
//LATIN_EXTENDED_A: '\u0020' .. '\u017E';

ESC_CHAR : '\\"' | '\\\\' ; // 2-char sequences \" and \\
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
EQUAL:'==';
NOT_EQUAL:'!=';
LARGER:'>';
LARGER_OR_EQUAL:'>=';
SMALLER:'<';
SMALLER_OR_EQUAL:'<=';

NEWLINE:'\r'?'\n'->skip;
// white space
WS:(' '|'\t')+ -> skip;
