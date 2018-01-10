package me.snov.sns.actor

import akka.actor.{Actor, ActorLogging, Props}
import akka.camel.{CamelMessage,Oneway}

import spray.json._

import me.snov.sns.model.Message

object RawProducerActor {
  def props(endpoint: String, subscriptionArn: String, topicArn: String) = Props(classOf[RawProducerActor], endpoint, subscriptionArn, topicArn)
}

class RawProducerActor(endpoint: String, subscriptionArn: String, topicArn: String) extends Actor with Oneway with ActorLogging {
  def endpointUri = endpoint

  override def transformOutgoingMessage(msg: Any) = msg match {
    case snsMsg: Message => {
      new CamelMessage(snsMsg.bodies.getOrElse("default", ""), Map(

        CamelMessage.MessageExchangeId -> snsMsg.uuid.toString,
        "x-amz-sns-message-type" -> "Notification",
        "x-amz-sns-message-id" -> snsMsg.uuid.toString,
        "x-amz-sns-subscription-arn" -> subscriptionArn,
        "x-amz-sns-topic-arn" -> topicArn
      ) ++ snsMsg.messageAttributes.map { m => m._1 -> m._2.stringValue }
      )
    }
  }
}
