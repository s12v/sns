package me.snov.sns.actor

import akka.actor.{Actor, ActorLogging, Props}
import me.snov.sns.model.{Subscription, Topic}
import me.snov.sns.service.DbService
import spray.json._

object DbActor {
  def props(dbService: DbService) = Props(new DbActor(dbService))

  case class CmdSaveSubscriptions(subscriptions: Iterable[Subscription])

  case class CmdSaveTopics(topics: Iterable[Topic])
}

class DbActor(dbService: DbService) extends Actor with ActorLogging {
  import me.snov.sns.actor.DbActor._

  var subscriptionsJson: JsValue = JsObject() 
  var topicsJson: JsValue = JsObject() 
  
  def saveSubscriptions(subscriptions: Iterable[Subscription]) = {
    subscriptionsJson = subscriptions.toJson
    save()
  }
  
  def saveTopics(topics: Iterable[Topic]) = {
    topicsJson = topics.toJson
    save()
  }
  
  private def save() = {
    dbService.save(subscriptionsJson, topicsJson)
  }
  
  override def receive = {
    case CmdSaveSubscriptions(subscriptions) => saveSubscriptions(subscriptions)
    case CmdSaveTopics(topics) => saveTopics(topics)
  }
}
