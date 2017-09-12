package com.xosmig.scalahw.calculator.parser

import com.xosmig.scalahw.calculator.lexer.Token

case class ParenthesesDontMatchException(token: Token,
                                         cause: Throwable = null)
  extends Exception(s"Parenthesis '$token' at position ${token.position} has no pair", cause)

