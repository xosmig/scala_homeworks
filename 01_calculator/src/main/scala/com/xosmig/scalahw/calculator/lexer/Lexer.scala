package com.xosmig.scalahw.calculator.lexer

trait Lexer {
  def intoTokens(s: String): List[Token]
}
