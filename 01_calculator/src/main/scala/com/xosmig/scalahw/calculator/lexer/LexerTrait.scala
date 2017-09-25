package com.xosmig.scalahw.calculator.lexer

trait LexerTrait {
  def intoTokens(s: String): List[Token]
}
