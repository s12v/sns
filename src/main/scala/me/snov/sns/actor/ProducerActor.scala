package me.snov.sns.actor

import akka.actor.{Actor, ActorLogging, Props}
import akka.camel.Producer

object ProducerActor {
  def props(endpoint: String) = Props(new ProducerActor(endpoint))
}

class ProducerActor(endpoint: String) extends Actor with Producer with ActorLogging {
  def endpointUri = endpoint
  override def oneway = true
}
