package me.snov.sns.api

import akka.actor.{Props, ActorRef, Actor}
import akka.pattern.ask
import akka.http.scaladsl.model.{ContentTypes, HttpEntity, HttpResponse}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server._
import akka.util.Timeout

object HomeApi {
  def props = Props[HomeActor]

  def route(actorRef: ActorRef)(implicit timeout: Timeout): Route = {
    pathSingleSlash {
      complete { (actorRef ? CmdHello).mapTo[HttpResponse] }
    }
  }
  
  case class CmdHello()
}

class HomeActor extends Actor {
  import HomeApi._

  def hello(): HttpResponse = {
    HttpResponse(entity = HttpEntity(ContentTypes.`text/html(UTF-8)`, "Hello, Akka"))
  }
  
  override def receive = {
    case CmdHello => sender ! hello()
    case _ => sender ! HttpResponse(500, entity = "Invalid message")
  }
}
