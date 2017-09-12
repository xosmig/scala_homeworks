package com.xosmig.scalahw.calculator.parser.expression

sealed trait ParsedToken {
  val canGoAfter: List[Class[_ <: ParsedToken]]
}

sealed trait ParsedOperationOrBracket extends ParsedToken

trait OpeningBracketTrait extends ParsedOperationOrBracket {
  override val canGoAfter: List[Class[_ <: ParsedToken]] = List(classOf[OpeningBracketTrait],
    classOf[ParsedBinaryOperation], classOf[ParsedUnaryOperation], classOf[ParsedFunction])
}

object OpeningBracket extends OpeningBracketTrait

class ClosingBracketTrait extends ParsedToken {
  override val canGoAfter: List[Class[_ <: ParsedToken]] = ??? //List(OpeningBracket, ClosingBracket, ParsedValue)
}

object ClosingBracket extends ClosingBracketTrait

class ParsedValue extends ParsedToken {
  override val canGoAfter: List[Class[_ <: ParsedToken]] = ??? //List(OpeningBracket, ParsedBinaryOperation, ParsedUnaryOperation)
}

trait ParsedOperation extends ParsedOperationOrBracket {
  def priority: Int
}

trait ParsedBinaryOperation extends ParsedOperation {
  override val canGoAfter: List[Class[_ <: ParsedToken]] = ??? //List(ClosingBracket, ParsedValue)
  def create(lhs: Expression, rhs: Expression): Operation
}

trait ParsedUnaryOperation extends ParsedOperation {
  override val canGoAfter: List[Class[_ <: ParsedToken]] = ??? //List(OpeningBracket, ParsedBinaryOperation)
  def create(rhs: Expression): Operation
}

trait ParsedFunction extends ParsedOperation {
  override val canGoAfter: List[Class[_ <: ParsedToken]] = ??? //List(OpeningBracket, ParsedBinaryOperation, ParsedUnaryOperation)
  def create(rhs: Expression): Operation
}
