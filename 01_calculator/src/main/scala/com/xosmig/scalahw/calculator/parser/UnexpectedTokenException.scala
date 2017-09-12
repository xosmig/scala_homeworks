package com.xosmig.scalahw.calculator.parser

import com.xosmig.scalahw.calculator.lexer.Token

case class UnexpectedTokenException(token: Token,
                                    cause: Throwable = null)
  extends Exception(s"Unexpected token: '$token' at position: ${token.position}", cause)

