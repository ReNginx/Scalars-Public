# Scalars Decaf Compiler Specifications

## IR Implementation Proposal

### Motivation

So far, our AST keeps track of the following information for each node:

- ANTLR token type
- ANTLR token name for virtual tokens
- Token text for non-virtual tokens
- Row/column info for non-virtual tokens

We need 3 hierarchies for IR:

1. IR Nodes hierarchy (i.e. `IrExpression` is the superclass of `IrBinopExpr`, as demonstrated in the handout)
2. Parse hierarchy
3. Scope hierarchy

### IR Nodes Hierarchy

An advantage about the nested classes approach (as suggested in the handout) is streamlined type checking and code generation. For example, we only need to a single checking/codegen/optimization definition for the class of all binary operators, rather than a separate one for each of them.

### Parse Hierarchy

[WIP] (Convert AST to IRT, instantiate each node of the IRT from its corresponding AST node, using an appropriate class, etc.)

- Go over `import`, create a `method descriptor` for each ID (might need to mark them as external function), and add them to global symbol table.
- Do the same to `field_decl`.
- Parse `method_decl` one by one. Do the following things in order.
  - Create a `method descriptor` by its `return type`, `name`, `param_list`, add it to `env_stack`
  - Parse the `BLK` part. store the result in `IrMethod.code`.
- Parse `if, while for` pretty much the same way. 
- For `IrContinue, IrBreak`, it need to be associated with its closest loop. (Could be solved by using a param in traversal function to keep track of cloest loop).
- For `IrReturn` it need to associate with its function.
- `IrExpr` contains a `type` variable, represents it's evaluated type.

### Scope Hierarchy

All "traversal" mentioned in this section refers to preorder traversal (root, left, right).

We discussed on Sunday that the scope hierarchy can be taken care of via a static symbol table, since Decaf does not support nested method declaration. However, now that I realized that--it can't! Nested scopes are possible, since each block defines a local scope, and nested `{}`s are allowed. Therefore, an environment stack is necessary to keep track of the scope hierarchy. The good news is that the environment stack can be created fairly simply using the symbol table.

#### Example

Observe the following snippet:

```c
int g_a = 1;
int g_b = 2;

void oscar (int i) {
    int o_a = 3;
}

void november (int i) {
    int n_a = 4
    {
        int n_b = 5;
    }
    mike(n_a);
}

void main () {
    int m_a = 6;
    november(g_a);
}
```

Since we've eliminated the operators as methods gimmick, the symbol tables resemble the following:

| Global           | main        | november    | november.block | oscar       |
| ---------------- | ----------- | ----------- | -------------- | ----------- |
| `desc(g_a)`      | `desc(m_a)` | `desc(n_a)` | `desc(n_b)`    | `desc(o_a)` |
| `desc(g_b)`      |             |             |                |             |
| `desc(oscar)`    |             |             |                |             |
| `desc(november)` |             |             |                |             |
| `desc(main)`     |             |             |                |             |

Note that the symbol tables should be anonymous, in that it should not contain a `name` field, nor should we identify a symbol table by the name of the scope associated with it. This is because block declarations only use `{}`, and are anonymous. How should we maintain pointers to all these symbol tables then? We use the environment stack.

#### Building the Environment Stack

When traversing the IRT, we maintain an environment stack. Each element of the stack contains a pointer to a symbol table. The stack should support the following operations:

- `push(t)`: Create an element containing a pointer to symbol table `t`, and push the element onto the stack.
- `pop()`: Pop the topmost element off the stack. Note that this method does not necessarily require returning the element itself. You just need to delete it!
- `top()`: Return the symbol table pointer of the topmost element. 

Additionally, each element of the stack should support a `next()` method, that returns the next (+1​ depth) element of the stack.

#### Building Symbol Tables

Building the symbol tables is easy. When we traverse the IRT we do the following:

- Whenever we enter a scope (method declaration, `{}`, `for`, `if`, `while`, etc., we initialize an empty symbol table, and push it onto the environment stack.
- Whenever we exit a scope, we call `pop()` on the environment stack.
- Whenever we encounter a variable/method declaration, we add its corresponding descriptor to `envStack.top()`.

#### An Integrated API for Symbol Table Building

Combining the previous two sections, we can see that it is convenient to extend the environment stack API to support the following:

- `add(desc)`: Add the descriptor `desc` to `this.top()`.
- `lookup(name)`: Return the descriptor corresponding to string `name`, return `none` if found nothing. The lookup procedure is as follows:
  - Call `this.top().lookup(name)`. If the symbol table returns a `desc`, then return `desc`. Otherwise (if the return is `none`, enter `this.top().next()` and call `lookup(name)`). Recurse until reaching the bottom of the stack. 

#### Checking "Declaration before Usage"

We check for this rule at the same time when building the stack. Whenever we see a usage for `name`, we perform `envstack.lookup(name)` and outputs a violation if the return is `none`.

### 