package me.snov.sns.actor

import akka.actor.Status.{Failure, Success}
import akka.actor.{Actor, ActorRef, Props}
import me.snov.sns.model.Topic

object TopicActor {
  def props(dbActor: ActorRef) = Props(new TopicActor(dbActor))

  case class CmdCreate(name: String)

  case class CmdDelete(arn: String)

  case class CmdList()

  case class CmdArns()
}

class TopicActor(dbActor: ActorRef) extends Actor {
  import me.snov.sns.actor.TopicActor._
  
  var topics = Map[String, Topic]()

  private def findOrCreateTopic(name: String): Topic = {
    topics.values.find(_.name == name) match {
      case Some(topic) => topic
      case None =>
        val topic = Topic(s"arn:aws:sns:us-east-1:${System.currentTimeMillis}:$name", name)
        topics += (topic.arn -> topic)
        topic
    }
  }
  
  private def delete(arn: String) = {
    if (topics.isDefinedAt(arn)) {
      topics -= arn
      Success
    } else {
      Failure
    }
  }

  override def receive = {
    case CmdCreate(name) => sender ! findOrCreateTopic(name)
    case CmdDelete(arn) => sender ! delete(arn)
    case CmdList => sender ! topics.values
    case CmdArns => sender ! topics.keys
  }
}
