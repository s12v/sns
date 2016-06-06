package me.snov.sns.actor

import akka.actor.Status.{Failure, Success}
import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import akka.util.Timeout
import me.snov.sns.actor.DbActor.CmdConfiguration
import me.snov.sns.model.{Configuration, Topic}

import scala.concurrent.duration._

object TopicActor {
  def props(dbActor: ActorRef) = Props(new TopicActor(dbActor))

  case class CmdCreate(name: String)

  case class CmdDelete(arn: String)

  case class CmdList()

  case class CmdArns()
}

class TopicActor(dbActor: ActorRef) extends Actor with ActorLogging {
  import me.snov.sns.actor.DbActor.CmdSaveTopics
  import me.snov.sns.actor.TopicActor._
  implicit val timeout = new Timeout(1.second)
  implicit val ec = context
  
  var topics = Map[String, Topic]()

  dbActor ! CmdConfiguration

  def load(configuration: Configuration) = {
    var topics = Map[String, Topic]()
    configuration.topics.foreach { topic =>
      topics += (topic.arn -> topic)
    }
    
    this.topics = topics
    log.info("loaded topics")
    log.info(this.topics.toString())
  }
  
  private def findOrCreateTopic(name: String): Topic = {
    topics.values.find(_.name == name) match {
      case Some(topic) => topic
      case None =>
        val topic = Topic(s"arn:aws:sns:us-east-1:${System.currentTimeMillis}:$name", name)
        topics += (topic.arn -> topic)
        
        dbActor ! CmdSaveTopics(topics.values.toList)
        
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
    case c: Configuration => load(c)
  }
}
