package com.xosmig.scalahw.calculator.parser.expression

case class UnaryMinus(arg: Expression) extends UnaryOperation(arg) {
  override def evaluate(): Value = Value(-arg.evaluate().value)
}
