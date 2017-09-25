package com.xosmig.scalahw.calculator.parser

import com.xosmig.scalahw.calculator.lexer.Token
import com.xosmig.scalahw.calculator.parser.expression.Expression

trait Parser {
  def parse(source: List[Token]): Expression
}
