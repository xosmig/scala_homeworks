package com.xosmig.scalahw.calculator.lexer

sealed trait Token {
  val position: Int
}

case class ValueToken(value: Double, position: Int) extends Token {
  override def toString: String = value.toString
}

case class StringToken(name: String, position: Int) extends Token {
  override def toString: String = name
}
