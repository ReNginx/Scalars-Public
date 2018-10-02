header {
package edu.mit.compilers.grammar;
}

options
{
  mangleLiteralPrefix = "TK_";
  language = "Java";
}

{@SuppressWarnings("unchecked")}
class DecafScanner extends Lexer;
options
{
  k = 2;
}

tokens
{
  "bool";
  "break";
  "class";
  "continue";
  "else";
  "false";
  "for";
  "if";
  "import";
  "int";
  "len";
  "return";
  "true";
  "void";
  "while";
}

// Selectively turns on debug tracing mode.
// You can insert arbitrary Java code into your parser/lexer this way.
{
  /** Whether to display debug information. */
  private boolean trace = false;

  public void setTrace(boolean shouldTrace) {
    trace = shouldTrace;
  }
  @Override
  public void traceIn(String rname) throws CharStreamException {
    if (trace) {
      super.traceIn(rname);
    }
  }
  @Override
  public void traceOut(String rname) throws CharStreamException {
    if (trace) {
      super.traceOut(rname);
    }
  }
}

////////// ////////// ////////// ////////// //////////

SC_ID:
  ( options{greedy=true;} : ALPHABET )+
  (ALPHABET | DIGIT)* ;

// GROUPERS
L_CURLY: '{' ;
R_CURLY: '}' ;
L_PARENTH: '(' ;
R_PARENTH: ')' ;
L_BRACKET: '[' ;
R_BRACKET: ']' ;

// ARITHMETIC
PLUS: '+' ;
MINUS: '-' ;
INCREMENT: '+' '+' ;
DECREMENT: '-' '-' ;
MULTIPLY: '*' ;
DIVIDE: '/' ;
MOD: '%' ;

// LOGICAL
AND: '&' '&' ;
OR: '|' '|' ;
EQUALS: '=' '=';
NEQUALS: '!' '=';
LESS_THAN: '<' ;
LESS_THAN_OR_EQ: '<' '=';
GREATER_THAN: '>' ;
GREATER_THAN_OR_EQ: '>' '=' ;
NOT: '!' ;

// PRIMITIVES
HEXADECIMAL: '0' 'x' ( DIGIT | 'a'..'f' | 'A'..'F' )+ ;
DECIMAL: ( DIGIT )+ ;
CHAR_LITERAL: '\'' CHAR '\'' ;
STR_LITERAL: '\"' (CHAR)* '\"' ;

// ASSIGNMENT
ASSIGN: '=' ;
PLUS_ASSIGN: '+' '=' ;
MINUS_ASSIGN: '-' '=' ;

// OTHER PUNCTUATIONS
COLON: ':' ;
COMMA: ',' ;
QUESTION: '?' ;
SEMICOLON: ';' ;

// IGNORE
SAME_LINE_COMMENT: '/' '/' ( ~('\r'|'\n') )* LINE_BREAK
  { _ttype = Token.SKIP; };
MULTI_LINE_COMMENT:
  '/' '*'
    ( options{greedy=true;} : (.) )*
  '*' '/'
  { _ttype = Token.SKIP; };
WHITESPACE: (' ' | '\u0009' | LINE_BREAK )
  { _ttype = Token.SKIP; };

// BASIC
protected ALPHABET: ( 'a'..'z' | 'A'..'Z' | '_' ) ;
protected CHAR: ( ESC | ~('\''|'\"'|'\r'|'\n'|'\t'|'\\') ) ;
protected DIGIT: '0'..'9' ;
protected ESC:  '\\' ('n' | 't' | '\"' | '\'' | '\\') ;
protected LINE_BREAK: ( '\n' | '\r' ) { newline(); };
