package me.snov.sns.config

import akka.actor.{ActorLogging, Actor, Props, ActorRef}
import akka.pattern.ask
import akka.util.Timeout
import me.snov.sns.config.DbActor.CmdSave
import spray.json._
import scala.concurrent.duration._

object DbActor {
  def props = Props[DbActor]

  case class CmdSave()
}

class DbActor(subscribeActor: ActorRef, topicActor: ActorRef) extends Actor with ActorLogging {
  implicit val timeout = new Timeout(1.second)
  
  def save() = {
    
//    val configurations = subscribeActor ? CmdSubscriptionConfigurations()
    
//    subscriptions.foreach((s: Subscription) => {
//      log.info(s.configuration.toJson.toString())
//    })
  }

  override def receive = {
    case CmdSave() => sender ! save()
  }
}
