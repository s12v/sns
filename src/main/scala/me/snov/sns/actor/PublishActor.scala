package me.snov.sns.actor

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import me.snov.sns.actor.SubscribeActor.CmdFanOut
import me.snov.sns.model.Message

object PublishActor {
  def props(actor: ActorRef) = Props(classOf[PublishActor], actor)

  case class CmdPublish(topicArn: String, bodies: Map[String, String])
}

class PublishActor(subscribeActor: ActorRef) extends Actor with ActorLogging {
  import me.snov.sns.actor.PublishActor._
  
  private def publish(topicArn: String, bodies: Map[String, String]): Message = {
    val message = Message(bodies)

    // todo ask
    subscribeActor ! CmdFanOut(topicArn, message)
    
    message
  }
  
  override def receive = {
    case CmdPublish(topicArn, bodies) => sender ! publish(topicArn, bodies)
  }
}
