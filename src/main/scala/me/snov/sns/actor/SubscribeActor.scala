package me.snov.sns.actor

import akka.actor.Status.{Success, Failure}
import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import akka.http.scaladsl.model.HttpResponse
import me.snov.sns.model.{Message, Subscription}

import scala.collection.mutable

object SubscribeActor {
  def props(dbActor: ActorRef) = Props(new SubscribeActor(dbActor))

  case class CmdSubscribe(topicArn: String, protocol: String, endpoint: String)

  case class CmdList()
  
  case class CmdListByTopic(topicArn: String)

  case class CmdFanOut(topicArn: String, message: Message)
}

class SubscribeActor(dbActor: ActorRef) extends Actor with ActorLogging {
  import me.snov.sns.actor.SubscribeActor._

  // todo immutable
  var subscriptions = mutable.HashMap.empty[String, List[Subscription]]

  private def fanOut(topicArn: String, message: Message) = {
    subscriptions.get(topicArn) match {
      case Some(ss: List[Subscription]) =>
        ss.foreach((s: Subscription) => {
          log.debug(s"Sending message ${message.uuid} to ${s.endpoint}")
          s.actorRef ! message.body
        })
        Success
      case None =>
        log.warning(s"Topic not found: $topicArn")
        Failure
    }
  }

  private def subscribe(topicArn: String, protocol: String, endpoint: String): Subscription = {
    val producer = context.system.actorOf(ProducerActor.props(endpoint))
    val subscription = new Subscription(topicArn, protocol, endpoint, producer)
    subscriptions.put(
      subscription.topicArn,
      subscription :: subscriptions.getOrElse(subscription.topicArn, List())
    )

//    dbActor ! CmdSave(subscriptions.values.flatten)
    
    subscription
  }

  private def listByTopic(topicArn: String): Iterable[Subscription] = {
    subscriptions.getOrElse(topicArn, List())
  }

  private def list(): Iterable[Subscription] = {
    subscriptions.values.flatten
  }

  override def receive = {
    case CmdSubscribe(topicArn, protocol, endpoint) => sender ! subscribe(topicArn, protocol, endpoint)
    case CmdListByTopic(topicArn) => sender ! listByTopic(topicArn)
    case CmdList() => sender ! list()
    case CmdFanOut(topicArn, message) => sender ! fanOut(topicArn, message)
    case _ => sender ! HttpResponse(500, entity = "Invalid message")
  }
}
