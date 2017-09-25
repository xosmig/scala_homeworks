package com.xosmig.scalahw
package collectorbot
package database

import akka.persistence.PersistentActor

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer

case class UserId(value: Int)
case class User(id: UserId, username: String)

class Transaction(val payerId: UserId, val payeeId: UserId, val amount: Float, val comment: String)
                 (private implicit val usersDB: UsersDB) {
  def payer: String = usersDB(payerId)
  def payee: String = usersDB(payeeId)

  def toFancyString = s"$payer заплатил за $payee $amount '$comment'"
}

private class TransactionsDB {
  private val map: mutable.HashMap[UserId, mutable.HashMap[UserId, ArrayBuffer[Transaction]]] = mutable.HashMap.empty

  private def applyMutable(payerId: UserId) = map.getOrElseUpdate(payerId, mutable.HashMap.empty)
  private def applyMutable(payerId: UserId, payeeId: UserId): mutable.Buffer[Transaction] =
    applyMutable(payerId).getOrElseUpdate(payeeId, ArrayBuffer.empty)

  def apply(payerId: UserId): collection.Map[UserId, ArrayBuffer[Transaction]] = applyMutable(payerId)

  def apply(payerId: UserId, payeeId: UserId): Seq[Transaction] = applyMutable(payerId, payeeId)

  def += (transaction: Transaction): Unit =
    applyMutable(transaction.payerId, transaction.payeeId) += transaction
}

private class UsersDB {
  private val idToName: mutable.HashMap[UserId, String] = mutable.HashMap.empty
  private val nameToId: mutable.HashMap[String, UserId] = mutable.HashMap.empty

  def += (user: User): Unit = {
    nameToId(user.username) = user.id
    idToName(user.id) = user.username
  }

  def apply(id: UserId): String = idToName(id)
  def apply(name: String)(implicit id: UserId): UserId = if (name == "me") { id } else { nameToId(name) }

  def contains(id: UserId): Boolean = idToName.contains(id)
  def contains(name: String): Boolean = name == "me" || nameToId.contains(name)
}

class CollectorDbActor extends PersistentActor {
  import CollectorDbActor._

  private val transactions = new TransactionsDB
  private val users = new UsersDB

  def receiveEvent(event: Event): Unit = {
    implicit val id: UserId = event.id
    event match {
      case AddTransaction(payer, payee, amount, comment) =>
        if (!users.contains(payer)) {
          sender ! UserNotFound(payer)
        } else if (!users.contains(payee)) {
          sender ! UserNotFound(payee)
        } else {
          val transaction = new Transaction(users(payer), users(payee), amount, comment)(users)
          transactions += transaction
          sender ! transaction
        }
      case RegisterName(name) =>
        if (users.contains(name)) {
          sender ! UsernameTaken(name)
        } else {
          users += User(event.id, name)
          sender ! ()
        }
    }
  }

  override def receiveRecover: Receive = {
    case evt: Event => receiveEvent(evt)
  }

  override def receiveCommand: Receive = {
    case request: Request =>
      implicit val id: UserId = request.id
      request match {
        case event: Event => persist(event)(receiveEvent)
        case request: GetUsername =>
          if (users.contains(request.id)) {
            sender ! FoundUsername(Some(users(id)))
          } else {
            sender ! FoundUsername(None)
          }
        case GetTransactions(peer) =>
          if (!users.contains(peer)) {
            sender ! UserNotFound(peer)
          } else {
            sender ! Transactions(transactions(id, users(peer)))
          }
        case GetAllTransactions() =>
          sender ! Transactions(transactions(id).flatMap { case (_, array) => array.toList })
      }
  }

  override def persistenceId = "com-xosmig-scalahw-collectorbot-database"
}

object CollectorDbActor {

  sealed trait Request {
    implicit val id: UserId
  }

  // events
  sealed trait Event extends Request

  case class AddTransaction(payer: String, payee: String, amount: Float, comment: String)(implicit val id: UserId)
    extends Event
  case class RegisterName(name: String)(implicit val id: UserId) extends Event

  // queries
  case class GetAllTransactions()(implicit val id: UserId) extends Request
  case class GetTransactions(peerName: String)(implicit val id: UserId) extends Request
  case class GetUsername()(implicit val id: UserId) extends Request

  // replies
  case class UserNotFound(name: String)
  case class UsernameTaken(name: String)
  case class Transactions(transactions: Iterable[Transaction]) {
    def toFancyString: String = transactions.map { _.toFancyString }.mkString("\n")
  }
  case class FoundUsername(name: Option[String])
}
