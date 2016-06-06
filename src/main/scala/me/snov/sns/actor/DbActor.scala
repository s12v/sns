package me.snov.sns.actor

import akka.actor.{ActorRef, Actor, ActorLogging, Props}
import me.snov.sns.model.{Configuration, Subscription, Topic}
import me.snov.sns.service.DbService

object DbActor {
  def props(dbService: DbService) = Props(new DbActor(dbService))

  case class CmdSaveSubscriptions(subscriptions: List[Subscription])

  case class CmdSaveTopics(topics: List[Topic])

  case class CmdConfiguration()
}

class DbActor(dbService: DbService) extends Actor with ActorLogging {
  import me.snov.sns.actor.DbActor._

  val configuration = dbService.load() 
  var subscriptions = List[Subscription]()
  var topics = List[Topic]()
  
  def saveSubscriptions(subscriptions: List[Subscription]) = {
    this.subscriptions = subscriptions
    save()
  }
  
  def saveTopics(topics: List[Topic]) = {
    this.topics = topics
    save()
  }
  
  def replyWithConfiguration(actorRef: ActorRef) = {
    if (configuration.isDefined) {
      actorRef ! configuration.get
    }
  }
  
  private def save() = {
    dbService.save(new Configuration(subscriptions = subscriptions, topics = topics))
  }
  
  override def receive = {
    case CmdConfiguration => replyWithConfiguration(sender)
    case CmdSaveSubscriptions(s) => saveSubscriptions(s)
    case CmdSaveTopics(t) => saveTopics(t)
  }
}
