package com.xosmig.scalahw.calculator.parser.expression

sealed trait ParsedToken {
  def canGoAfter(token: ParsedToken): Boolean
}

sealed trait ParsedOperationOrBracket extends ParsedToken

//sealed trait OpeningBracketTrait extends  {

  //  List[Class[_ <: ParsedToken]] = List
  //(classOf[OpeningBracketTrait],
    //classOf[ParsedBinaryOperation], classOf[ParsedUnaryOperation], classOf[ParsedFunction])
//}

object OpeningBracket extends ParsedOperationOrBracket {
  override def canGoAfter(token: ParsedToken): Boolean = token match {
    case OpeningBracket | _: ParsedBinaryOperation | _: ParsedUnaryOperation | _: ParsedFunction => true
    case _ => false
  }
}

object ClosingBracket extends ParsedOperationOrBracket {
  override def canGoAfter(token: ParsedToken): Boolean = token match {
    case ClosingBracket | ParsedValue => true
    case _ => false
  }
}

object ParsedValue extends ParsedToken {
  override def canGoAfter(token: ParsedToken): Boolean = token match {
    case OpeningBracket | _: ParsedBinaryOperation | _: ParsedUnaryOperation => true
    case _ => false
  }
}

trait ParsedOperation extends ParsedOperationOrBracket {
  def priority: Int
  def 
}

trait ParsedBinaryOperation extends ParsedOperation {
  override def canGoAfter(token: ParsedToken): Boolean = token match {
    case ClosingBracket | ParsedValue | _: ParsedOperation => true
    case _ => false
  }
  def create(lhs: Expression, rhs: Expression): Operation
}

trait ParsedUnaryOperation extends ParsedOperation {
  override def canGoAfter(token: ParsedToken): Boolean = token match {
    case OpeningBracket | _: ParsedBinaryOperation => true
    case _ => false
  }
  def create(rhs: Expression): Operation
}

trait ParsedFunction extends ParsedOperation {
  override def canGoAfter(token: ParsedToken): Boolean = token match {
    case OpeningBracket | _: ParsedBinaryOperation | _: ParsedUnaryOperation => true
    case _ => false
  }
  def create(rhs: Expression): Operation
}
