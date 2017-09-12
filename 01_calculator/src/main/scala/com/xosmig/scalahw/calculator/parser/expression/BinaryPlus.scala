package com.xosmig.scalahw.calculator.parser.expression

case class BinaryPlus(lhs: Expression, rhs: Expression) extends BinaryOperation(lhs, rhs)  {
  override def evaluate() = Value(lhs.evaluate().value + rhs.evaluate.value)
}

object ParsedBinaryPlus extends ParsedBinaryOperation
