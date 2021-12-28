lexer grammar CommonLexerRules;
// tokenizer rules are in upper case
ID:[a-zA-Z]+;
INT:[0-9]+;
NEWLINE:'\r'? '\n';
WS: [ \t]+ -> skip;