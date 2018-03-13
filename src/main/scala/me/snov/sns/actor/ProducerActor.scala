package me.snov.sns.actor

import akka.actor.{Actor, ActorLogging, Props}
import akka.camel.{CamelMessage, Oneway}
import spray.json._
import me.snov.sns.model.Message
import org.apache.camel.Exchange
import org.apache.camel.component.http.HttpMethods

object ProducerActor {
  def props(endpoint: String, subscriptionArn: String, topicArn: String) = Props(classOf[ProducerActor], endpoint, subscriptionArn, topicArn)
}

class ProducerActor(endpoint: String, subscriptionArn: String, topicArn: String) extends Actor with Oneway with ActorLogging {
  def endpointUri = endpoint

  override def transformOutgoingMessage(msg: Any) = msg match {
    case snsMsg: Message => {
      new CamelMessage(snsMsg.toJson.toString, Map(

        CamelMessage.MessageExchangeId -> snsMsg.uuid,
        Exchange.HTTP_METHOD -> HttpMethods.POST,
        "x-amz-sns-message-type" -> "Notification",
        "x-amz-sns-message-id" -> snsMsg.uuid,
        "x-amz-sns-subscription-arn" -> subscriptionArn,
        "x-amz-sns-topic-arn" -> topicArn
      ))
    }
  }
}
