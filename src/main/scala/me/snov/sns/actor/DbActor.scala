package me.snov.sns.actor

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import me.snov.sns.model.Configuration
import me.snov.sns.service.DbService

object DbActor {
  def props(dbService: DbService) = Props(classOf[DbActor], dbService)

  case class CmdGetConfiguration()
}

class DbActor(dbService: DbService) extends Actor with ActorLogging {
  import me.snov.sns.actor.DbActor._

  val configuration = dbService.load() 
  
  def replyWithConfiguration(actorRef: ActorRef) = {
    if (configuration.isDefined) {
      actorRef ! configuration.get
    }
  }
  
  override def receive = {
    case CmdGetConfiguration => replyWithConfiguration(sender)
    case c: Configuration => dbService.save(c)
  }
}
