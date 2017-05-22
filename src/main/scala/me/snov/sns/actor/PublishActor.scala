package me.snov.sns.actor

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import me.snov.sns.actor.SubscribeActor.CmdFanOut
import me.snov.sns.model.Message

object PublishActor {
  def props(actor: ActorRef) = Props(classOf[PublishActor], actor)

  case class CmdPublish(topicArn: String, body: String)
}

class PublishActor(subscribeActor: ActorRef) extends Actor with ActorLogging {
  import me.snov.sns.actor.PublishActor._
  
  private def publish(topicArn: String, body: String): Message = {
    val message = Message(body)

    // todo ask
    subscribeActor ! CmdFanOut(topicArn, message)
    
    message
  }
  
  override def receive = {
    case CmdPublish(topicArn, message) => sender ! publish(topicArn, message)
  }
}
