package me.snov.sns.api

import akka.actor.ActorRef
import akka.actor.Status.Success
import akka.http.scaladsl.model.HttpResponse
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.pattern.ask
import akka.util.Timeout
import me.snov.sns.actor.SubscribeActor.{CmdListTopics, CmdDeleteTopic, CmdCreateTopic}
import me.snov.sns.model.Topic
import me.snov.sns.response.TopicResponse

import scala.concurrent.ExecutionContext

object TopicApi {
  private val namePattern = """([\w+_-]{1,256})""".r
  private val arnPattern = """([\w+_:-]{1,512})""".r

  def route(actor: ActorRef)(implicit timeout: Timeout, ec: ExecutionContext): Route = {
    pathSingleSlash {
      formField('Action ! "CreateTopic") {
        formField('Name) {
          case namePattern(name) => complete {
            (actor ? CmdCreateTopic(name)).mapTo[Topic].map {
              TopicResponse.create
            }
          }
          case _ => complete(HttpResponse(400, entity = "InvalidParameter: invalid topic name"))
        } ~
        complete(HttpResponse(400, entity = "Topic name is missing"))
      } ~
      formField('Action ! "DeleteTopic") {
        formField('TopicArn) {
          case arnPattern(arn) => complete {
            (actor ? CmdDeleteTopic(arn)).map {
              case Success => TopicResponse.delete
              case _ => HttpResponse(404, entity = "NotFound")
            }
          }
          case _ => complete(HttpResponse(400, entity = "Invalid topic ARN"))
        } ~
        complete(HttpResponse(404, entity = "NotFound"))
      } ~ 
      formField('Action ! "ListTopics") {
        complete {
          (actor ? CmdListTopics).mapTo[Iterable[Topic]].map {
            TopicResponse.list
          }
        }
      }
    }
  }
}
