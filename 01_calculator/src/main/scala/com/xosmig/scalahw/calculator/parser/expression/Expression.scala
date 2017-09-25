package com.xosmig.scalahw.calculator.parser.expression

sealed trait Expression {
  def evaluate(): Value
}

case class Value(value: Double) extends Expression {
  override def evaluate(): Value = this
}

sealed trait Operation extends Expression
abstract class BinaryOperation(lhs: Expression, rhs: Expression) extends Operation
abstract class UnaryOperation(rhs: Expression) extends Operation
abstract class Function(rhs: Expression) extends Operation
