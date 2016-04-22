package me.snov.sns.api

import akka.actor.{Actor, ActorLogging, Props}
import akka.camel.Producer

object SnsProducer {
  def props(endpoint: String) = Props(new SnsProducer(endpoint))
}

class SnsProducer(endpoint: String) extends Actor with Producer with ActorLogging {
  def endpointUri = endpoint
  override def oneway = true
}
