package me.snov.sns.api

import akka.actor.ActorRef
import akka.actor.Status.Success
import akka.http.scaladsl.model.HttpResponse
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.pattern.ask
import akka.util.Timeout
import me.snov.sns.actor.SubscribeActor.{CmdListSubscriptions, CmdListSubscriptionsByTopic, CmdSubscribe, CmdUnsubscribe}
import me.snov.sns.model.Subscription
import me.snov.sns.response.SubscribeResponse

import scala.concurrent.ExecutionContext

object SubscribeApi {
  private val arnPattern = """([\w+_:-]{1,512})""".r

  def route(actorRef: ActorRef)(implicit timeout: Timeout, ec: ExecutionContext): Route = {
    pathSingleSlash {
      formField('Action ! "Subscribe") {
        formFields('Endpoint, 'Protocol, 'TopicArn) { (endpoint, protocol, topicArn) =>
          complete {
            (actorRef ? CmdSubscribe(topicArn, protocol, endpoint)).mapTo[Subscription] map {
              SubscribeResponse.subscribe
            }
          }
        } ~
          complete(HttpResponse(400, entity = "Endpoint, Protocol, TopicArn are required"))
      } ~
        formField('Action ! "ListSubscriptionsByTopic") {
          formField('TopicArn) {
            case arnPattern(topicArn) => complete {
              (actorRef ? CmdListSubscriptionsByTopic(topicArn)).mapTo[Iterable[Subscription]] map {
                SubscribeResponse.listByTopic
              }
            }
            case _ => complete(HttpResponse(400, entity = "Invalid topic ARN"))
          } ~
            complete(HttpResponse(400, entity = "TopicArn is missing"))
        } ~
        formField('Action ! "ListSubscriptions") {
          complete {
            (actorRef ? CmdListSubscriptions()).mapTo[Iterable[Subscription]] map {
              SubscribeResponse.list
            }
          }
        } ~
        formField('Action ! "Unsubscribe") {
          formField('SubscriptionArn) { (arn) =>
            complete {
              (actorRef ? CmdUnsubscribe(arn)).map {
                case Success => SubscribeResponse.unsubscribe
                case _ => HttpResponse(404, entity = "NotFound")
              }
            }
          } ~
          complete(HttpResponse(400, entity = "SubscriptionArn is missing"))
        }
    }
  }
}
