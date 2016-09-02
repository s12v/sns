package me.snov.sns.actor

import akka.actor.{Actor, Props}
import akka.http.scaladsl.model.{HttpEntity, HttpResponse}

object HomeActor {
  def props = Props[HomeActor]

  case class CmdHello()
}

class HomeActor extends Actor {
  import me.snov.sns.actor.HomeActor._

  def hello = HttpResponse(entity = HttpEntity("Hello, Akka"))

  override def receive = {
    case CmdHello => sender ! hello
    case _ => sender ! HttpResponse(500, entity = "Invalid message")
  }
}
