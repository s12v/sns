package me.snov.sns.actor

import java.util.UUID

import akka.actor.Status.{Failure, Success}
import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import me.snov.sns.actor.DbActor.CmdSaveSubscriptions
import me.snov.sns.model.{Message, Subscription}

object SubscribeActor {
  def props(dbActor: ActorRef) = Props(new SubscribeActor(dbActor))

  case class CmdSubscribe(topicArn: String, protocol: String, endpoint: String)

  case class CmdList()
  
  case class CmdListByTopic(topicArn: String)

  case class CmdFanOut(topicArn: String, message: Message)
}

class SubscribeActor(dbActor: ActorRef) extends Actor with ActorLogging {
  import me.snov.sns.actor.SubscribeActor._

  var subscriptions = Map[String, List[Subscription]]()
  var actorPool = Map[Subscription, ActorRef]()

  private def fanOut(topicArn: String, message: Message) = {
    try {
      subscriptions.get(topicArn) match {
        case Some(ss: List[Subscription]) =>
          ss.foreach((s: Subscription) => {
            if (actorPool.contains(s)) {
              log.debug(s"Sending message ${message.uuid} to ${s.endpoint}")
              actorPool(s) ! message.body
            } else {
              throw new RuntimeException(s"No actor for subscription ${s.endpoint}")
            }
          })
        case None => throw new RuntimeException(s"Topic not found: $topicArn")
      }
      
      Success
    } catch {
      case e: RuntimeException => Failure
    }
  }

  private def subscribe(topicArn: String, protocol: String, endpoint: String): Subscription = {
    val producer = context.system.actorOf(ProducerActor.props(endpoint))
    val subscription = new Subscription(UUID.randomUUID().toString, "", topicArn, protocol, endpoint)
    val listByTopic = subscription :: subscriptions.getOrElse(subscription.topicArn, List())

    actorPool += (subscription -> producer)
    subscriptions += (subscription.topicArn -> listByTopic)

    dbActor ! CmdSaveSubscriptions(list())
    
    subscription
  }

  private def listByTopic(topicArn: String): Iterable[Subscription] = {
    subscriptions.getOrElse(topicArn, List())
  }

  private def list(): List[Subscription] = {
    subscriptions.values.flatten.toList
  }

  override def receive = {
    case CmdSubscribe(topicArn, protocol, endpoint) => sender ! subscribe(topicArn, protocol, endpoint)
    case CmdListByTopic(topicArn) => sender ! listByTopic(topicArn)
    case CmdList() => sender ! list()
    case CmdFanOut(topicArn, message) => sender ! fanOut(topicArn, message)
  }
}
