package com.xosmig.scalahw.calculator.parser.expression

case class UnaryMinus(rhg: Expression) extends UnaryOperation(rhg) {
  override def evaluate(): Value = Value(-rhg.evaluate().value)
}

object ParsedUnaryMinus extends ParsedUnaryOperation {
  override def create(rhs: Expression): Operation = UnaryMinus(rhs)
  override def priority: Int = 30
}
