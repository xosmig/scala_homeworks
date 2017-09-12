package com.xosmig.scalahw.calculator.parser

import com.xosmig.scalahw.calculator.lexer.{IllegalTokenException, StringToken, Token, ValueToken}
import com.xosmig.scalahw.calculator.parser.expression._

object StandardParser extends Parser {
  override def parse(source: List[Token]): Expression = {
    if (source.isEmpty) {
      return Value(0)
    }

    var expressions = List[Expression]()
    var operations = List[ParsedOperationOrBracket]()

    def squashHighPriority(minPriorityExcluded: Int): Unit = {
      while (operations.nonEmpty && operations.last.isInstanceOf[ParsedOperation] &&
          operations.last.asInstanceOf[ParsedOperation].priority > minPriorityExcluded) {
        val rhs = expressions.last
        val lhs = if (expressions.tail.isEmpty) { null } else { expressions.tail.last }
        expressions ::= (operations.last.asInstanceOf[ParsedOperation] match {
          case binary: ParsedBinaryOperation =>
            expressions = expressions.drop(2)
            binary.create(lhs, rhs)
          case unary: ParsedUnaryOperation =>
            expressions = expressions.tail
            unary.create(rhs)
          case function: ParsedFunction =>
            expressions = expressions.tail
            function.create(rhs)
        })
      }
    }

    var lastToken: ParsedToken = OpeningBracket

    for (token <- source) {
      val parsedToken = token match {
        case ValueToken(value, _) =>
          expressions ::= Value(value)
          new ParsedValue
        case StringToken("(", _) =>
          operations ::= OpeningBracket
          OpeningBracket
        case StringToken(")", _) =>
          squashHighPriority(-1)
          if (operations.isEmpty) { throw ParenthesesDontMatchException(token) }
          assert(operations.last == OpeningBracket)
          operations = operations.tail
          ClosingBracket
        case _ =>
          val operation = (token match {
            case StringToken("+", _) => ParsedBinaryPlus
            // TODO
            case _ => throw IllegalTokenException(token)
          }).asInstanceOf[ParsedOperation]
          squashHighPriority(operation.priority)
          operations ::= operation
          operation
      }
      if (!parsedToken.canGoAfter.exists(cl => cl.isInstance(lastToken))) {
        throw UnexpectedTokenException(token)
      }
      lastToken = parsedToken
    }

    squashHighPriority(-1)
    assert(expressions.size == 1)
    expressions.last
  }
}
