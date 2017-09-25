package com.xosmig.scalahw.calculator.lexer

import org.junit.Assert._
import org.junit.Test

class LexerTest {
  @Test
  def AcceptsSingleToken(): Unit = {
    assertEquals(Lexer.intoTokens("foo"), List(StringToken("foo", 0)))
  }

  @Test
  def WorksOnSimpleValidExample(): Unit = {
    assertEquals(
      Lexer.intoTokens(" foo + bar **)( a1234  1234 "),
      List(StringToken("foo", 1), StringToken("+", 5), StringToken("bar", 7), StringToken("*", 11),
        StringToken("*", 12), StringToken(")", 13), StringToken("(", 14), StringToken("a1234", 16), ValueToken(1234, 23)))
  }

  @Test(expected = classOf[IllegalTokenException])
  def rejectsInvalidToken(): Unit = {
    Lexer.intoTokens("!@#$%^&*")
  }
}
