package com.xosmig.scalahw.calculator.parser

import com.xosmig.scalahw.calculator.lexer.{IllegalTokenException, StringToken, Token, ValueToken}
import com.xosmig.scalahw.calculator.parser.expression._

import scala.collection.mutable

object Parser extends ParserTrait {
  override def parse(source: List[Token]): Expression = {
    if (source.isEmpty) {
      return Value(0)
    }

    val expressions = mutable.Stack[Expression]()
    val operations = mutable.Stack[ParsedOperationOrBracket]()

    def squashHighPriority(minPriorityIncluded: Int): Unit = {
      while (operations.nonEmpty && operations.top.isInstanceOf[ParsedOperation] &&
          operations.top.asInstanceOf[ParsedOperation].priority >= minPriorityIncluded) {
        val rhs = expressions.top
        expressions.pop()
        expressions.push(operations.top.asInstanceOf[ParsedOperation] match {
          case binary: ParsedBinaryOperation =>
            val lhs = expressions.top
            expressions.pop()
            binary.create(lhs, rhs)
          case unary: ParsedUnaryOperation =>
            unary.create(rhs)
          case function: ParsedFunction =>
            function.create(rhs)
        })
        operations.pop()
      }
    }

    var lastToken: ParsedToken = OpeningBracket

    for (token <- source) {
      val parsedToken = token match {
        case ValueToken(value, _) =>
          expressions.push(Value(value))
          ParsedValue
        case StringToken("(", _) =>
          operations.push(OpeningBracket)
          OpeningBracket
        case StringToken(")", _) =>
          squashHighPriority(0)
          if (operations.isEmpty) { throw ParenthesesDontMatchException(token) }
          assert(operations.top == OpeningBracket)
          operations.pop()
          ClosingBracket
        case _ =>
          val operation = (token match {
            case StringToken("+", _) => ParsedBinaryPlus
            case StringToken("*", _) => ParsedBinaryTimes
//            case StringToken("-", _) =>
//              if ()
            case _ => throw IllegalTokenException(token)
          }).asInstanceOf[ParsedOperation]
          squashHighPriority(operation.priority)
          operations.push(operation)
          operation
      }
      if (!parsedToken.canGoAfter.exists(cl => cl.isInstance(lastToken))) {
        throw UnexpectedTokenException(token)
      }
      lastToken = parsedToken
    }

    squashHighPriority(0)
    assert(expressions.size == 1)
    expressions.top
  }
}
