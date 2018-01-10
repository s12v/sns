package me.snov.sns.actor

import java.util.UUID

import akka.actor.Status.{Failure, Success}
import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import me.snov.sns.actor.DbActor.CmdGetConfiguration
import me.snov.sns.model._
import akka.actor.PoisonPill
import akka.actor.Status

object SubscribeActor {
  def props(dbActor: ActorRef) = Props(classOf[SubscribeActor], dbActor)

  case class CmdSubscribe(topicArn: String, protocol: String, endpoint: String)

  case class CmdUnsubscribe(subscriptionArn: String)

  case class CmdListSubscriptions()

  case class CmdListSubscriptionsByTopic(topicArn: String)

  case class CmdFanOut(topicArn: String, message: Message)

  case class CmdCreateTopic(name: String)

  case class CmdDeleteTopic(arn: String)

  case class CmdListTopics()

  case class CmdSetSubscriptionAttributes(subscriptionArn: String, attributeName: String, attributeValue: String)
  case class CmdGetSubscriptionAttributes(subscriptionArn: String)
}

class SubscribeActor(dbActor: ActorRef) extends Actor with ActorLogging {

  import me.snov.sns.actor.SubscribeActor._
  
  type TopicArn = String
  type SubscriptionArn = String

  var topics = Map[TopicArn, Topic]()
  var subscriptions = Map[TopicArn, List[Subscription]]()
  var actorPool = Map[SubscriptionArn, ActorRef]()

  dbActor ! CmdGetConfiguration

  private def fanOut(topicArn: TopicArn, message: Message) = {
    try {
      if (topics.isDefinedAt(topicArn) && subscriptions.isDefinedAt(topicArn)) {
        subscriptions(topicArn).foreach((s: Subscription) => {
          if (actorPool.isDefinedAt(s.arn)) {
            log.debug(s"Sending message ${message.uuid} to ${s.endpoint}")
             actorPool(s.arn) ! message
          } else {
            throw new RuntimeException(s"No actor for subscription ${s.endpoint}")
          }
        })
      } else {
        throw new TopicNotFoundException(s"Topic not found: $topicArn")
      }
      
      Success
    } catch {
      case e: Throwable => Failure(e)
    }
  }

  def findSubscription(subscriptionArn: String): Option[Subscription] = {
    subscriptions.values.flatten.find{_.arn == subscriptionArn}
  }

  def producerFor(subscription: Subscription) = {
    val producer = if(subscription.isRawMessageDelivery) {
      context.system.actorOf(RawProducerActor.props(subscription.endpoint, subscription.arn, subscription.topicArn))
    } else {
      context.system.actorOf(ProducerActor.props(subscription.endpoint, subscription.arn, subscription.topicArn))
    }
    producer
  }
  def updateSubscription(subscription: Subscription) = {
    val updatedSubs = subscription :: subscriptions(subscription.topicArn).filter(_.arn != subscription.arn)
    subscriptions += (subscription.topicArn -> updatedSubs)

   val producer = producerFor(subscription)
    
    //update the producer
    actorPool += (subscription.arn -> producer)
  }

  def setAttribute(subscriptionArn: String, attributeName: String, attributeValue: String) = {
    log.info(s"Setting ${attributeName} to ${attributeValue} for ${subscriptionArn}")
    findSubscription(subscriptionArn).map { sub =>
      val updated = sub.copy(subscriptionAttributes = Some(sub.subscriptionAttributes.getOrElse(Map.empty[String,String]) + (attributeName -> attributeValue)))
      updateSubscription(updated)
      Success
    } getOrElse {
      log.error(s"No subscription found for ${subscriptionArn}")
       Failure(new Exception("Not Found"))
    }
  }
  def getAttributes(subscriptionArn: String): Option[Map[String,String]] = {
    findSubscription(subscriptionArn).flatMap { sub =>
      sub.subscriptionAttributes.map { attrs =>
        attrs ++ Map(
          "SubscriptionArn" -> sub.arn,
          "TopicArn" -> sub.topicArn,
          "Owner" -> sub.owner
        )
      }
    }
  }

  def subscribe(topicArn: TopicArn, protocol: String, endpoint: String): Subscription = {
    val subscription = Subscription(s"${topicArn}:${UUID.randomUUID}", "", topicArn, protocol, endpoint)
    initSubscription(subscription)
    
    save()

    subscription
  }

  def initSubscription(subscription: Subscription) = {
    val producer = producerFor(subscription)
    val listByTopic = subscription :: subscriptions.getOrElse(subscription.topicArn, List())

    actorPool += (subscription.arn -> producer)
    subscriptions += (subscription.topicArn -> listByTopic)
  }

  def unsubscribe(subscriptionArn: String) = {
    subscriptions = subscriptions.map { case (key, topicSubscriptions) => (key, topicSubscriptions.filter((s: Subscription) => s.arn != subscriptionArn)) }.toMap

    save()

    Success
  }

  def listSubscriptionsByTopic(topicArn: TopicArn): List[Subscription] = {
    subscriptions.getOrElse(topicArn, List())
  }

  def listSubscriptions(): List[Subscription] = {
    subscriptions.values.flatten.toList
  }
  
  def findOrCreateTopic(name: String): Topic = {
    topics.values.find(_.name == name) match {
      case Some(topic) => topic
      case None =>
        val topic = Topic(s"arn:aws:sns:us-east-1:123456789012:$name", name)
        topics += (topic.arn -> topic)

        save()

        topic
    }
  }

  def delete(arn: TopicArn) = {
    if (topics.isDefinedAt(arn)) {
      topics -= arn
      
      if (subscriptions.isDefinedAt(arn)) {
        subscriptions -= arn
      }

      save()

      Success
    } else {
      Failure
    }
  }


  def load(configuration: Configuration) = {
    configuration.topics.foreach { topic => topics += (topic.arn -> topic) }
    configuration.subscriptions.foreach { initSubscription }
    log.info("Loaded configuration")
  }
  
  def save() = {
    dbActor ! new Configuration(subscriptions = listSubscriptions(), topics = topics.values.toList)
  }

  override def receive = {
    case CmdCreateTopic(name) => sender ! findOrCreateTopic(name)
    case CmdDeleteTopic(arn) => sender ! delete(arn)
    case CmdListTopics => sender ! topics.values
    case CmdSubscribe(topicArn, protocol, endpoint) => sender ! subscribe(topicArn, protocol, endpoint)
    case CmdUnsubscribe(subscriptionArn) => sender ! unsubscribe(subscriptionArn)
    case CmdListSubscriptionsByTopic(topicArn) => sender ! listSubscriptionsByTopic(topicArn)
    case CmdListSubscriptions() => sender ! listSubscriptions()
    case CmdFanOut(topicArn, message) => sender ! fanOut(topicArn, message)
    case CmdSetSubscriptionAttributes(subscriptionArn, attributeName, attributeValue) => sender ! setAttribute(subscriptionArn, attributeName, attributeValue)
    case CmdGetSubscriptionAttributes(subscriptionArn) => sender ! getAttributes(subscriptionArn)
    case configuration: Configuration => load(configuration)
  }
}
