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

tokens
{
  ARGS;
  ARRAY;
  BLOCK;
  CONDITION;
  FIELD_DECLARATION;
  FIELD_LIST;
  IF_BLOCK;
  ELSE_BLOCK;
  FOR_START;
  FOR_UPDATE;
  HEX;
  ID;
  IF_NO;
  IF_YES;
  IMPORT;
  INDEX;
  INT;
  METHOD_CALL;
  METHOD_DECLARATION;
  PARAM_LIST;
  PARAMETER;
  PROGRAM;
  STATEMENT;
  TYPE;
  VAR;
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
  (import_decl)* (field_list)* (method_decl)* EOF!
  {#program = #(#[PROGRAM,"PROGRAM"], #program);}
);

import_decl: TK_import! id SEMICOLON!
  {#import_decl = #(#[IMPORT, "IMPORT"], #import_decl);};

field_list: type field_decl ( COMMA! field_decl )* SEMICOLON!
  {#field_list = #(#[FIELD_LIST, "FIELD_LIST"], #field_list);};
protected field_decl : (var | array)
  {#field_decl = #(#[FIELD_DECLARATION, "FIELD_DECLARATION"], #field_decl);};
protected var: id
  {#var = #(#[VAR, "VAR"], #var);};
protected array: id L_BRACKET! index R_BRACKET!
  {#array = #(#[ARRAY, "ARRAY"], #array);};
protected index: (
  (integer | hex)
  {#index = #(#[INDEX, "INDEX"], #index);}
);

method_decl: (
  return_type
  id
  L_PARENTH!
    param_list
  R_PARENTH!
  block
  {#method_decl = #(#[METHOD_DECLARATION, "METHOD_DECLARATION"], #method_decl);}
);
protected return_type: (
  ( TK_int | TK_bool | TK_void )
  {#return_type = #(#[TYPE, "TYPE"], #return_type);}
);
protected param_list: (
  (parameter (COMMA! parameter)*)?
  {#param_list = #(#[PARAM_LIST, "PARAM_LIST"], #param_list);}
);
protected parameter: (
  type id
  {#parameter = #(#[PARAMETER, "PARAMETER"], #parameter);}
);

block: (
  L_CURLY!
    ( field_list )*
    ( statement )*
  R_CURLY!
  {#block = #(#[BLOCK, "BLOCK"], #block);}
);

type: ( TK_int | TK_bool )
  {#type = #(#[TYPE, "TYPE"], #type);} ;

statement: (
    (
      location
      (
        ( // assign operation
          ( ASSIGN^ | PLUS_ASSIGN^ | MINUS_ASSIGN^ )
          expr
        )
        |
        ( INCREMENT^ | DECREMENT^ )
      )
      SEMICOLON!
    )
  | ( method_call SEMICOLON! )
  | ( if_else )
  | ( for_loop )
  | ( while_loop )
  | ( TK_return^  (expr)? SEMICOLON! )
  | ( TK_break            SEMICOLON! )
  | ( TK_continue         SEMICOLON! )
);

if_else: (
  TK_if^ L_PARENTH! condition R_PARENTH!
    if_block
  ( TK_else! else_block )?
);
if_block: (
  block
  { #if_block = #(#[IF_BLOCK, "IF_BLOCK"], #if_block); }
);
else_block: (
  block
  { #else_block = #(#[ELSE_BLOCK, "ELSE_BLOCK"], #else_block); }
);

for_loop: (
  TK_for^
    L_PARENTH!
      for_start SEMICOLON!
      condition   SEMICOLON!
      for_update
    R_PARENTH!
  block
);
protected for_start: (
  id ASSIGN^ expr
  { #for_start = #(#[FOR_START, "FOR_START"], #for_start); }
);
protected condition: (
  expr
  { #condition = #(#[CONDITION, "CONDITION"], #condition); }
);
protected for_update: (
  location
  (
      ( (PLUS_ASSIGN^ | MINUS_ASSIGN^) expr )
    | ( INCREMENT^ | DECREMENT^ )
  )
  { #for_update = #(#[FOR_UPDATE, "FOR_UPDATE"], #for_update); }
);

while_loop: (
  TK_while^
    L_PARENTH! condition R_PARENTH!
  block
);

method_call: (
  id
  L_PARENTH!
    method_args
  R_PARENTH!
  { #method_call = #(#[METHOD_CALL, "METHOD_CALL"], #method_call); }
);
method_args: (
  (method_arg (COMMA! method_arg)*)?
  { #method_args = #(#[ARGS, "ARGS"], #method_args); }
);
method_arg : ( expr | STR_LITERAL );

protected id: (
  SC_ID
  { #id = #(#[ID, "ID"], #id); }
);

location: id | expr_array ;
protected expr_array: (
  id L_BRACKET! expr_index R_BRACKET!
    {#expr_array = #(#[ARRAY, "ARRAY"], #expr_array);}
);
protected expr_index: (
  expr
  { #expr_index = #(#[INDEX, "INDEX"], #expr_index); }
);

protected hex: (
  HEXADECIMAL
  { #hex = #(#[HEX, "HEX"], #hex); }
);
protected integer: (
  DECIMAL
  { #integer = #(#[INT, "INT"], #integer); }
);


// shows hierarchy of precedence, where
// expr towards the bottom have higher precedence
stand_alone_expr: (  // stand-alone expression
    ( location )
  | ( method_call )
  | ( integer | hex | CHAR_LITERAL | TK_true | TK_false )  // literal
  | ( TK_len^ L_PARENTH! id R_PARENTH! )
  | ( MINUS^ expr )
  | ( (options{greedy=true;}: DECREMENT)+ expr )
  | ( NOT^ expr )
  | ( L_PARENTH! expr R_PARENTH! )
);
mul_op_expr: (
  stand_alone_expr
  ( options{greedy=true;}: (MULTIPLY^ | DIVIDE^ | MOD^) mul_op_expr )?
);
add_op_expr: (
  mul_op_expr
  ( options{greedy=true;}: (PLUS^ | MINUS^) add_op_expr )?
);
comparison_expr: (
  add_op_expr
  (
    options{greedy=true;}:
    (LESS_THAN^ | LESS_THAN_OR_EQ^ | GREATER_THAN^ | GREATER_THAN_OR_EQ^)
    comparison_expr
  )?
);
equality_expr: (
  comparison_expr
  ( options{greedy=true;}: (NEQUAL^ | EQUAL^) equality_expr )?
);
logical_operator: (
  equality_expr
  ( options{greedy=true;}: (AND^ | OR^) logical_operator )?
);
expr: (
  logical_operator
  ( options{greedy=true;}: QUESTION^ if_yes COLON! if_no )?
);
protected if_yes: (
  expr
  { #if_yes = #(#[IF_YES, "IF_YES"], #if_yes); }
);
protected if_no: (
  expr
  { #if_no = #(#[IF_NO, "IF_NO"], #if_no); }
);
