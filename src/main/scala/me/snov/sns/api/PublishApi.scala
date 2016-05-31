package me.snov.sns.api

import akka.actor.ActorRef
import akka.http.scaladsl.model.HttpResponse
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.pattern.ask
import akka.util.Timeout
import me.snov.sns.actor.PublishActor.CmdPublish
import me.snov.sns.model.Message
import me.snov.sns.response.PublishResponse

import scala.concurrent.ExecutionContext

object PublishApi {
  private val arnPattern = """([\w+_:-]{1,512})""".r
  
  def route(actorRef: ActorRef)(implicit timeout: Timeout, ec: ExecutionContext): Route = {
    pathSingleSlash {
      formField('Action ! "Publish") {
        formFields('TopicArn, 'Message) { (topicArn, message) =>
          topicArn match {
            case arnPattern(topic) => complete {
              (actorRef ? CmdPublish(topic, message)).mapTo[Message].map {
                PublishResponse.publish
              }
            }
            case _ => complete(HttpResponse(400, entity = "Invalid topic ARN"))
          }
        } ~
        complete(HttpResponse(400, entity = "TopicArn is required"))
      }
    }
  }
}
