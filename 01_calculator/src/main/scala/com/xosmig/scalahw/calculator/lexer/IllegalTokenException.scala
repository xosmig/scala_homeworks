package com.xosmig.scalahw.calculator.lexer

case class IllegalTokenException(token: Token, cause: Throwable = null)
    extends Exception(s"Illegal token: '$token' at position: ${token.position}", cause)
