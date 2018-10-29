# Assumptions For CFG

## CFG has the following types

- VirtualCFG
- CFGMethod
- CFGProgram
- CFGConditional
- CFGBlock
- CFGCall

## Assumptions for each type of CFG

### Universal

for a location specified below, it should either be a Variable, or a Array, whose `index` is a literal or a variable. This assumption is not handled properly at this moment. **WE MAY HAVE A BUG HERE**

### CFGConditional

- field `condtion` is a literal or a location.
- `next`, `ifFalse` should always be defined.

### CFGBlock

- statements could only be one of the following.
  - AssignStatement
    - field `eval` is a location
  - CompoundAssignStatement
    - field `eval` is a location
    - `value` is a location or a literal
  - Unary Operation
    - `eval` is a location
    - `expression` is a location or a literal
  - Binary Operation (except And, Or, they are handled by CFG conditional)
    - `eval` is a location
    - `lhs, rhs` are location or literal
  - `Ternary Opertion`  **DOES NOT APPEAR IN CFG BLOCK, THEY ARE TRANSFORMED INTO CFGCONDITIONAL** 