package me.snov.sns.api

import akka.actor.ActorRef
import akka.http.scaladsl.model.HttpResponse
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server._
import akka.pattern.ask
import akka.util.Timeout
import me.snov.sns.actor.HomeActor.CmdHello

object HomeApi {
  def route(actorRef: ActorRef)(implicit timeout: Timeout): Route = {
    pathSingleSlash {
      complete { (actorRef ? CmdHello).mapTo[HttpResponse] }
    }
  }
}

