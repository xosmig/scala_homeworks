package com.xosmig.scalahw.calculator.parser.expression

sealed trait ParsedToken {
  val canGoAfter: List[Class[_ <: ParsedToken]]
}

class OpeningBracket extends ParsedToken {
  override val canGoAfter: List[Class[_ <: ParsedToken]] = List(classOf[OpeningBracket],
    classOf[ParsedBinaryOperation], classOf[ParsedUnaryOperation], classOf[ParsedFunction])
}

class ClosingBracket extends ParsedToken {
  override val canGoAfter: List[Class[_ <: ParsedToken]] = ??? //List(OpeningBracket, ClosingBracket, ParsedValue)
}

class ParsedValue extends ParsedToken {
  override val canGoAfter: List[Class[_ <: ParsedToken]] = ??? //List(OpeningBracket, ParsedBinaryOperation, ParsedUnaryOperation)
}

trait ParsedOperation extends ParsedToken {
  def priority: Int
}

trait ParsedBinaryOperation extends ParsedToken {
  override val canGoAfter: List[Class[_ <: ParsedToken]] = ??? //List(ClosingBracket, ParsedValue)
  def create(lhs: Expression, rhs: Expression): Operation
}

trait ParsedUnaryOperation extends ParsedToken {
  override val canGoAfter: List[Class[_ <: ParsedToken]] = ??? //List(OpeningBracket, ParsedBinaryOperation)
  def create(rhs: Expression): Operation
}

trait ParsedFunction extends ParsedToken {
  override val canGoAfter: List[Class[_ <: ParsedToken]] = ??? //List(OpeningBracket, ParsedBinaryOperation, ParsedUnaryOperation)
  def create(rhs: Expression): Operation
}
