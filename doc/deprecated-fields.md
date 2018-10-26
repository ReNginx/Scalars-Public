# List of Deprecated Fields in IR Specification

The following fields have been initialized as empty, and should be scheduled for removal.

```scala
case class AssignStatement.valueBlock (Assignment.scala)
case class CompoundStatement.valueBlock (Assignment.scala)
case class MethodCall.paramBlocks (Call.scala)
case class Location.indexBlock (Expression.scala)
case class For.conditionBlock (Loop.scala)
case class While.conditionBlock (Loop.scala)
case class Return.valueBlock (Statement.scala)
case class If.conditionBlock (Statement.scala)
```

