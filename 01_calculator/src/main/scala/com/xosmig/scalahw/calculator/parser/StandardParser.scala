package com.xosmig.scalahw.calculator.parser

import com.sun.org.apache.xpath.internal.operations.UnaryOperation
import com.xosmig.scalahw.calculator.lexer.{IllegalTokenException, StringToken, Token, ValueToken}
import com.xosmig.scalahw.calculator.parser.expression._

import scala.util.control.Breaks._

class StandardParser extends Parser {
  override def parse(source: List[Token]): Expression = {
    var expressions = List[Expression]()
    var operations = List[ParsedToken]()

    def squashHighPriority(minPriority: Int): Unit = {
      while (operations.nonEmpty && operations.last.isInstanceOf[ParsedOperation] &&
          operations.last.asInstanceOf[ParsedOperation].priority >= minPriority) {
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

    var lastToken: ParsedToken = new OpeningBracket

    for (token <- source) {
      var isOperation = false
      val parsedToken = token match {
        case ValueToken(value, _) =>
          expressions ::= Value(value)
          new ParsedValue
        case StringToken(")", _) =>
          // TODO
          new ClosingBracket
        case _ =>
          val res = token match {
            case StringToken("(", _) => new OpeningBracket
            case StringToken("+", _) => ParsedBinaryPlus
            // TODO
            case _ => throw IllegalTokenException(token)
          }
          operations ::= res
          res
      }
      if (!parsedToken.canGoAfter.exists(cl => cl.isInstance(lastToken))) {
        throw UnexpectedTokenException(token)
      }
      lastToken = parsedToken
    }
    Value(1)
  }
}
