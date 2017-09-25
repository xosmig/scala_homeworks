package com.xosmig.scalahw
package collectorbot
package bot

import akka.actor.ActorRef
import akka.pattern.ask
import akka.util.Timeout
import com.xosmig.scalahw.collectorbot.database.CollectorDbActor._
import com.xosmig.scalahw.collectorbot.database.{Transaction, UserId}
import com.xosmig.scalahw.collectorbot.parser._
import com.xosmig.scalahw.collectorbot.bot.CollectorBot.HELP_MESSAGE
import info.mukel.telegrambot4s.api.declarative.Commands
import info.mukel.telegrambot4s.api.{Polling, TelegramBot}
import info.mukel.telegrambot4s.models.Message

import scala.collection.mutable
import scala.concurrent.duration.DurationInt
import scala.util.Success

class CollectorBot(val token: String, val database: ActorRef) extends TelegramBot with Polling with Commands {

  private trait Action {
    def apply(text: String)(implicit id: UserId, message: Message, timeout: Timeout): Unit
  }

  private def dbRequest(request: Any, lambda: Any => Unit)(implicit message: Message, timeout: Timeout): Unit = {
    (database ? request).onComplete {
      case Success(UserNotFound(name)) => reply(s"Error: user $name not found!")
      case Success(reply) => lambda(reply)
      case _ => reply("Ошибка базы данных :(")
    }
  }

  private object Register extends Action {
    override def apply(text: String)(implicit id: UserId, message: Message, timeout: Timeout): Unit = {
      text match {
        case MessageParser.userNameRegex() =>
          dbRequest(RegisterName(text), {
            case UsernameTaken(name) =>
              reply(s"Username $name is taken. Please, try another one.")
            case () =>
              reply("Ok")
              reply(HELP_MESSAGE)
              setAction(NextCommand)
          })
        case _ =>
          reply("Username должен удовлетворять регулярному выражению: " + MessageParser.userNameRegex.toString())
      }
    }
  }

  private object NextCommand extends Action {
    override def apply(text: String)(implicit id: UserId, message: Message, timeout: Timeout): Unit = {
      MessageParser.parse(text) match {
        case StartCommand =>
          dbRequest(GetUsername(), {
            case FoundUsername(Some(name)) =>
              reply(s"Привет, $name!")
            case FoundUsername(None) =>
              reply("Привет! Как я могу тебя называть?")
              setAction(Register)
          })
        case AddCommand(payer, payees, amount, comment) =>
          for (payee <- payees) {
            dbRequest(AddTransaction(payer, payee, amount / payees.size, comment), {
              case tr: Transaction =>
                reply(tr.toFancyString)
            })
          }
        case SumCommand =>
          dbRequest(GetAllTransactions(), {
            case Transactions(transactions) =>
                val income = transactions.filter { _.payerId == id }.foldLeft(0.0)(_ + _.amount)
                val outcome = transactions.filter { _.payeeId == id }.foldLeft(0.0)(_ + _.amount)
                reply(s"Итог: ${income - outcome}")
          })
        case AllCommand =>
          dbRequest(GetAllTransactions(), {
            case tr: Transactions => reply(tr.toFancyString)
          })
        case TransactionsCommand(peer) =>
          dbRequest(GetTransactions(peer), {
            case tr: Transactions => reply(tr.toFancyString)
          })
        case _ =>
          reply("Чё?")
          reply(HELP_MESSAGE)
      }
    }
  }

  private val currentAction: mutable.HashMap[Long, Action] = mutable.HashMap.empty

  private def setAction(action: Action)(implicit message: Message): Unit = {
    currentAction(message.chat.id) = action
  }

  onMessage {
    implicit message =>
      message.text.foreach { text =>
        message.from.foreach { sender =>
          implicit val id: UserId = UserId(sender.id)
          implicit val timeout: Timeout = Timeout(1.second)
          currentAction.getOrElseUpdate(message.chat.id, NextCommand)(text)
        }
      }
  }
}

object CollectorBot {
  val HELP_MESSAGE: String =
    """
      |Инструкция:
      |Во всех коммандах можно использовать слово me вместо своего логина.
      |Везде нужно использовать логины, выбранные при первом запуске данного бота.
      |/start - запустить бота или узнать свой username.
      |/transactions <peer> - просмотреть все транзакции между вами и <peer>.
      |/all - просмотреть все свои транзакции.
      |/sum - сумма долгов вам (может быть меньше 0).
      |/add <payer> <amount> <должник1> [<должник2> ...] - <payer> заплатил <amount> за должников.
      |Долг будет разделен поровну между должниками. <payer> может сам быть в списке должников.
    """.stripMargin
}
