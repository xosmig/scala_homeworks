package com.xosmig.scalahw.calculator.parser.expression

case class Sine(arg: Expression) extends Function(arg) {
  override def evaluate(): Value = Value(math.sin(arg.evaluate().value))
}
