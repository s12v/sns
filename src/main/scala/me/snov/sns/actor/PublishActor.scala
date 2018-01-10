package me.snov.sns.actor

import akka.actor.Status.{Failure, Success}
import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import akka.pattern.ask
import akka.pattern.pipe
import akka.util.Timeout
import me.snov.sns.actor.SubscribeActor.CmdFanOut
import me.snov.sns.model.{Message, MessageAttribute}

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

object PublishActor {
  def props(actor: ActorRef) = Props(classOf[PublishActor], actor)

  case class CmdPublish(topicArn: String, bodies: Map[String, String], messageAttributes: Map[String, MessageAttribute])
}

class PublishActor(subscribeActor: ActorRef) extends Actor with ActorLogging {
  import me.snov.sns.actor.PublishActor._

  private implicit val timeout = Timeout(1.second)
  private implicit val ec = context.dispatcher

  private def publish(topicArn: String, bodies: Map[String, String], messageAttributes: Map[String, MessageAttribute])(implicit ec: ExecutionContext) = {
    val message = Message(bodies, messageAttributes = messageAttributes)

    (subscribeActor ? CmdFanOut(topicArn, message)).map {
      case Failure(e) => Failure(e)
      case Success => message
    }
  }

  override def receive = {
    case CmdPublish(topicArn, bodies, attributes) => publish(topicArn, bodies, attributes) pipeTo sender
  }
}
