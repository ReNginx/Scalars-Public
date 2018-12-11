# Scalars Decaf Compiler

Dec. 11, 2018 06:30 AM

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

## Dataflow Analysis

- 100% pass on public tests
- 100% pass on private tests

## Optimization

### Current Progress

|        Name         | Implemented | Tested | Array Support  | RepeatOptimization Support |
| :-----------------: | :---------: | :----: | :------------: | :------------------------: |
|      Local CSE      |     Yes     |  Yes   |      Yes       |            Yes             |
|      Local CP       |     Yes     |  Yes   | Not Applicable |            Yes             |
|      Local DCE      |     Yes     |  Yes   | Not Applicable |            Yes             |
|     Global CSE      |     Yes     |  Yes   |      Yes       |            Yes             |
|      Global CP      |     Yes     |  Yes   |      Yes       |            Yes             |
|     Global DCE      |     Yes     |  Yes   | Not Applicable |            Yes             |
|  Constant Folding   |     Yes     |  Yes   | Not Applicable |            Yes             |
| Invariant Hoisting  |     Yes     |  Yes   |       No       |            Yes             |
| Register Allocation |     Yes     |  Yes   |       No       |            Yes             |

- 100% pass on optimizer tests
- 100% pass on derby tests

