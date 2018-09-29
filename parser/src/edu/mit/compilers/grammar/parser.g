header {
package edu.mit.compilers.grammar;
}

options
{
  mangleLiteralPrefix = "TK_";
  language = "Java";
}

class DecafParser extends Parser;
options
{
  importVocab = DecafScanner;
  k = 3;
  buildAST = true;
}

// Java glue code that makes error reporting easier.
// You can insert arbitrary Java code into your parser/lexer this way.
{
  // Do our own reporting of errors so the parser can return a non-zero status
  // if any errors are detected.
  /** Reports if any errors were reported during parse. */
  private boolean error;

  @Override
  public void reportError (RecognitionException ex) {
    // Print the error via some kind of error reporting mechanism.
    error = true;
  }
  @Override
  public void reportError (String s) {
    // Print the error via some kind of error reporting mechanism.
    error = true;
  }
  public boolean getError () {
    return error;
  }

  // Selectively turns on debug mode.

  /** Whether to display debug information. */
  private boolean trace = false;

  public void setTrace(boolean shouldTrace) {
    trace = shouldTrace;
  }
  @Override
  public void traceIn(String rname) throws TokenStreamException {
    if (trace) {
      super.traceIn(rname);
    }
  }
  @Override
  public void traceOut(String rname) throws TokenStreamException {
    if (trace) {
      super.traceOut(rname);
    }
  }
}

////////// ////////// ////////// ////////// //////////

// ORDER SHOWN BELOW MIRRORS THE ORDER ON SPEC SHEET

program: (
  ( import_decl )*
  ( field_decl )*
  ( method_decl )*
  EOF
);

import_decl: TK_import id SEMICOLON ;

field_decl: type var_or_array ( COMMA var_or_array )* SEMICOLON ;
protected var_or_array: id ( L_BRACKET int_literal R_BRACKET )? ;

method_decl: (
  ( type | TK_void ) id
  L_PARENTH
    ( type id (COMMA type id)* )?
  R_PARENTH
  block
);

block: (
  L_CURLY
    ( field_decl )*
    ( statement )*
  R_CURLY
);

type: ( TK_int | TK_bool );

statement: (
    ( location assign_expr SEMICOLON )
  | ( method_call SEMICOLON )
  | ( if_else )
  | ( for_loop )
  | ( while_loop )
  | ( TK_return   (expr)? SEMICOLON )
  | ( TK_break            SEMICOLON )
  | ( TK_continue         SEMICOLON )
);

if_else: (
  TK_if L_PARENTH expr R_PARENTH
    block
  ( TK_else block )?
);

for_loop: (
  TK_for
    L_PARENTH
      id EQ expr SEMICOLON
      expr       SEMICOLON
      location (
          ( compount_assign_op expr )
        | ( increment )
      )
    R_PARENTH
  block
);

while_loop: (
  TK_while
    L_PARENTH expr R_PARENTH
  block
);

assign_expr: (
    ( assign_op expr )
  | ( increment )
);

assign_op: ( EQ | compount_assign_op );

compount_assign_op: ( PLUS_EQ | MINUS_EQ );

increment: ( INCREMENT | DECREMENT );

method_call: (
  method_name
  L_PARENTH
    ( method_param (COMMA method_param)* )?
  R_PARENTH
);
method_param : ( expr | string_literal ) ;

method_name: id ;

location: id ( L_BRACKET expr R_BRACKET )? ;

expr: (
  expr_left
  ( options{greedy=true;} : expr_right )?
);
expr_left: (
    ( location )
  | ( method_call )
  | ( literal )
  | ( TK_len L_PARENTH id R_PARENTH )
  | ( MINUS expr )
  | ( EXCLAMATION expr )
  | ( L_PARENTH expr R_PARENTH )
);
expr_right: (
  expr_left_of_right
  ( options{greedy=true;} : expr_right)?
) ;
expr_left_of_right: (
    ( bin_op expr )
  | ( QUESTION expr COLON expr )
);

bin_op: ( arith_op | rel_op | eq_op | cond_op );

arith_op: ( MINUS | PLUS | MULTIPLY | DIVIDE | MOD );

rel_op: ( LESS_THAN | LESS_THAN_OR_EQ | GREATER_THAN | GREATER_THAN_OR_EQ );

eq_op: ( NEQUALS | EQUALS );

cond_op: ( AND | OR );

literal: ( int_literal | char_literal | bool_literal );
id: IDENTIFIER ;
int_literal: ( decimal_literal | hex_literal );
decimal_literal: DECIMAL ;
hex_literal: HEX ;
bool_literal: ( TK_true | TK_false );
char_literal: CHAR_LITERAL ;
string_literal: STR_LITERAL ;
