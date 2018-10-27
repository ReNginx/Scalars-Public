# Scalars Decaf Compiler

Oct. 26, 2018 23:52 PM

## Scanner

- 100% pass on public tests
- 100% pass on private tests

## Parser

- 100% pass on public tests
- 100% pass on private tests

## IR/Semantics

- 100% pass on public tests
- 100% pass on private tests

## Code Generation

- Not completed
- Please see `./doc/report_3-unoptimized_code_generation` for detail

### What Currently Works

- Expression tree flattening
- Creating control flow graph from IR

### What Currently Don't Work

- Generate code from control flow graph

### What To Do

- Re-design part of control flow graph generation
- Re-base code generation upon the new CFG structure.