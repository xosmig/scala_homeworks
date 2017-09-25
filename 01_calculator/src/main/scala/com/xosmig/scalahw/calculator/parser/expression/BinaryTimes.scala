package com.xosmig.scalahw.calculator.parser.expression

case class BinaryTimes(lhs: Expression, rhs: Expression) extends BinaryOperation(lhs, rhs)  {
  override def evaluate() = Value(lhs.evaluate().value + rhs.evaluate().value)
}

object ParsedBinaryTimes extends ParsedBinaryOperation {
  override def priority: Int = 20
  override def create(lhs: Expression, rhs: Expression): Operation = BinaryTimes(lhs, rhs)
}
