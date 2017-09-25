package com.xosmig.scalahw
package collectorbot

import akka.actor.{ActorSystem, Props}
import com.typesafe.akka.extension.quartz.QuartzSchedulerExtension
import com.xosmig.scalahw.collectorbot.bot.CollectorBot
import com.xosmig.scalahw.collectorbot.database.CollectorDbActor

object Main extends App {
  // @XoCollectorBot
  val token = "424026110:AAFXpzTQQrlrhC9rnHEPT5IlSOpnxtO2SKU"

  val system = ActorSystem()
  val scheduler = QuartzSchedulerExtension(system)
  val database = system.actorOf(Props(classOf[CollectorDbActor]))

  private val bot = new CollectorBot(token, database)

  bot.run()
}
