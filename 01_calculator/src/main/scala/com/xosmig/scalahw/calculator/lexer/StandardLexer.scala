package com.xosmig.scalahw.calculator.lexer

import scala.collection.mutable.ArrayBuffer

object StandardLexer extends Lexer {
  override def intoTokens(source: String): List[Token] = {
    val operationR = raw"(\+|\-|\*|/|[a-zA-Z_][a-zA-Z_0-9]*|\(|\))"
    val valueR = raw"([1-9]\d*)"
    val spaceR = raw"(\s+)"
    val trashR = raw"(.+)"
    val commonRegex = raw"(?:$operationR|$valueR|$spaceR|$trashR)".r

    val result = ArrayBuffer[Token]()
    for (m <- commonRegex.findAllMatchIn(source)) {
      m match {
        case commonRegex(opName, null, null, null) => result += StringToken(opName, m.start)
        case commonRegex(null, value, null, null)  => result += ValueToken(value.toDouble, m.start)
        case commonRegex(null, null, _, null)      =>
        case commonRegex(null, null, null, trash)  => throw IllegalTokenException(StringToken(trash, m.start))
      }
    }

    result.toList
  }
}
