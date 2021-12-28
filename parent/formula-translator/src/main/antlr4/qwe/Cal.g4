
grammar Cal;
import CommonLexerRules;

prog: stat+;

stat: expr NEWLINE  # printExpr
    | ID '=' expr NEWLINE #assign
    | NEWLINE #blank
    | 'clear' #clear
    ;
// syntax rule are in lower case
expr: expr op=('*'|'/') expr #MulDiv
    | expr op=('+'|'-') expr #AddSub
    | INT #int
    | ID #id
    | '(' expr ')' #parens

    ;

MUL: '*';
DIV: '/';
ADD: '+';
SUB: '-';
