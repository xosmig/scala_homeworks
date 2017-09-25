package com.xosmig.scalahw.collectorbot.parser

sealed trait UserMessage

case class AddCommand(payer: String, payees: List[String], amount: Float, comment: String) extends UserMessage

case class TransactionsCommand(peer: String) extends UserMessage

case object StartCommand extends UserMessage

case object SumCommand extends UserMessage

case object AllCommand extends UserMessage

case object WrongMessage extends UserMessage
