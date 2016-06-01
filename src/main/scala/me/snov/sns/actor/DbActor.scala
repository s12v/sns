package me.snov.sns.actor

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import me.snov.sns.model.SubscriptionJsonProtocol._
import me.snov.sns.model.{Subscription, Topic}
import spray.json._

object DbActor {
  def props = Props[DbActor]

  case class CmdSaveSubscriptions(subscriptions: Iterable[Subscription])

  case class CmdSaveTopics(topics: Iterable[Topic])
}

class DbActor extends Actor with ActorLogging {
  import me.snov.sns.actor.DbActor._

  var subscriptionsJson: JsValue = JsObject() 
  var topicsJson: JsValue = JsObject() 
  
  def saveSubscriptions(subscriptions: Iterable[Subscription]) = {
    subscriptionsJson = subscriptions.toJson
    log.info(subscriptionsJson.toString())
  }
  
  def saveTopics(topics: Iterable[Topic]) = {
//    val s = topics.toJson.toString()
//    topics.foreach(subscription => {
//      log.info(s)
//    })
  }
  
  private def saveToFile() = {
    val configuration = new JsObject(Map(
      "subscriptions" -> subscriptionsJson,
      "topics" -> topicsJson
    ))
    val configurationString = configuration.toString()
    // todo save to file
  }

  override def receive = {
    case CmdSaveSubscriptions(subscriptions) => saveSubscriptions(subscriptions)
    case CmdSaveTopics(topics) => saveTopics(topics)
  }
}
