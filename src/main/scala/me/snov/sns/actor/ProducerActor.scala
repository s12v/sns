package me.snov.sns.actor

import akka.actor.{Actor, ActorLogging, Props}
import akka.camel.Oneway

object ProducerActor {
  def props(endpoint: String) = Props(classOf[ProducerActor], endpoint)
}

class ProducerActor(endpoint: String) extends Actor with Oneway with ActorLogging {
  def endpointUri = endpoint
}
