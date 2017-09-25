package com.xosmig.scalahw
package collectorbot
package parser

import scala.util.matching.Regex
import scala.util.parsing.combinator.RegexParsers

class MessageParser extends RegexParsers {
  override def skipWhitespace = true

  override val whiteSpace: Regex = raw"\s+".r

  val userNameRegex: Regex = raw"[a-zA-Z_а-яА-Я][a-zA-Z_0-9а-яА-Я]*".r
  val userNameParser: Parser[String] =  userNameRegex
  val floatParser: Parser[Float] = raw"[-+]?[0-9]+\.?[0-9]*".r ^^ { _.toFloat }
  val messageParser: Parser[String] = "[^\"]*".r

  val addTransactions: Parser[UserMessage] =
    "/add" ~> opt(userNameParser) ~ floatParser ~ rep1(userNameParser) ~ opt("\"" ~> messageParser <~ "\"") ^^ {
      case payer ~ amount ~ payees ~ message =>
        AddCommand(payer.getOrElse("me"), payees, amount, message.getOrElse(""))
    }

  val startCommand: Parser[UserMessage] = "/start" ^^ { _ => StartCommand }

  val getSum: Parser[UserMessage] = "/sum" ^^ {_ => SumCommand }

  val getTransactions: Parser[UserMessage] = "/transactions" ~> userNameParser ^^ {
    peerName => TransactionsCommand(peerName)
  }

  val getAllTransactions: Parser[UserMessage] = "/all" ^^ {_ => AllCommand}

  // TODO: add more commands
  val userMessage: Parser[UserMessage] = addTransactions | startCommand | getSum | getAllTransactions | getTransactions
}

object MessageParser extends MessageParser {
  def parse(text: String): UserMessage = {
    parse(userMessage, text) match {
      case Success(message, _) => message
      case _ => WrongMessage
    }
  }
}
