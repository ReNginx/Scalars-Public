# Scalars Decaf Compiler

Nov. 2, 2018 23:01 PM

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

- 100% pass on public tests
- 100% pass on private tests

## Dataflow

- Implemented: local CSE. local CP, local DCE, global CSE, global CP, global DCE (see Optimization section)
- 100% pass on public tests
- 100% pass on private tests

## Optimization

### Current Progress

|              Name              | Implemented | Tested | Array Support  |   RepeatOptimization Support    |
| :----------------------------: | :---------: | :----: | :------------: | :-----------------------------: |
|           Local CSE            |     Yes     |  Yes   |      Yes       | Not Applicable (not idempotent) |
|            Local CP            |     Yes     |  Yes   | Not Applicable |               Yes               |
|           Local DCE            |     Yes     |  Yes   | Not Applicable |               Yes               |
|           Global CSE           |     Yes     |  Yes   |      WIP       |               WIP               |
|           Global CP            |     Yes     |  Yes   |      WIP       |               WIP               |
|           Global DCE           |     Yes     |  Yes   |      WIP       |               WIP               |
|        Constant Folding        |     Yes     |        |                |                                 |
|       Invariant Hoisting       |     Yes     |        |                |                                 |
| Induction Variable Elimination |     WIP     |        |                |                                 |

