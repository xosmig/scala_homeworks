package com.xosmig.scalahw.calculator.parser

import com.xosmig.scalahw.calculator.lexer.{StringToken, ValueToken}
import com.xosmig.scalahw.calculator.parser.expression.{BinaryPlus, BinaryTimes, UnaryMinus, Value}
import org.junit.Assert._
import org.junit.Test

class ParserTest {
  @Test
  def ParsesSingleValue(): Unit = {
    // 42
    assertEquals(Value(42), Parser.parse(List(ValueToken(42, 0))))
  }

  @Test
  def ParsesSingleBinaryOperator(): Unit = {
    /// 1 + 2
    val expression = List(ValueToken(1, 0), StringToken("+", 10), ValueToken(2, 20))
    assertEquals(BinaryPlus(Value(1), Value(2)), Parser.parse(expression))
  }

  @Test
  def ParsesParentheses(): Unit = {
    // 1 + (2 + 3 + 4) + 5
    val expression = List(ValueToken(1, 0), StringToken("+", 0), StringToken("(", 0), ValueToken(2, 0),
      StringToken("+", 0), ValueToken(3, 0), StringToken("+", 0), ValueToken(4, 0), StringToken(")", 0),
      StringToken("+", 0), ValueToken(5, 0))
    val parsedExpression = BinaryPlus(
      BinaryPlus(
        Value(1.0),
        BinaryPlus(
          BinaryPlus(Value(2.0), Value(3.0)),
          Value(4.0)
        )
      ),
      Value(5.0)
    )
    assertEquals(parsedExpression, Parser.parse(expression))
  }

  @Test
  def ParsesPriority(): Unit = {
    // 1 * 2 + 3 * 4
    val expression = List(ValueToken(1, 0), StringToken("*", 0), ValueToken(2, 0), StringToken("+", 0),
      ValueToken(3, 0), StringToken("*", 0), ValueToken(4, 0))
    val parsedExpression = BinaryPlus(BinaryTimes(Value(1), Value(2)), BinaryTimes(Value(3), Value(4)))
    assertEquals(parsedExpression, Parser.parse(expression))
  }

  @Test
  def ParsesSimpleUnaryMinus(): Unit = {
    // -42
    val expression = List(StringToken("-", 0), ValueToken(42, 0))
    assertEquals(UnaryMinus(Value(42)), Parser.parse(expression))
  }
}
