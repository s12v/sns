package me.snov.sns.api

import java.util.UUID

import akka.actor.{Actor, ActorRef, Props}
import akka.http.scaladsl.model.{FormData, HttpResponse}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.pattern.ask
import akka.util.Timeout

import scala.collection.mutable

object TopicApi {
  private val namePattern = """([\w+_-]{1,256})""".r
  private val arnPattern = """([\w+_:-]{1,512})""".r

  def route(actorRef: ActorRef)(implicit timeout: Timeout): Route = {
    pathSingleSlash {
      entity(as[FormData]) { entity =>
        entity.fields.get("Action") match {
          case Some("CreateTopic") =>
            entity.fields.getOrElse("Name", "") match {
              case namePattern(name) => complete {
                (actorRef ? CmdCreate(name)).mapTo[HttpResponse]
              }
              case _ => complete(HttpResponse(400, entity = "InvalidParameter: invalid topic name"))
            }
          case Some("DeleteTopic") =>
            entity.fields.getOrElse("TopicArn", "") match {
              case arnPattern(name) => complete {
                (actorRef ? CmdDelete(name)).mapTo[HttpResponse]
              }
              case _ => complete(HttpResponse(400, entity = "InvalidParameter: Invalid topic arn"))
            }
          case Some("ListTopics") =>
            complete { (actorRef ? CmdList).mapTo[HttpResponse] }
          case default => reject()
        }
      }
    }
  }

  case class Topic(val arn: String, val name: String)

  case class CmdCreate(name: String)

  case class CmdDelete(arn: String)

  case class CmdList()
}

object TopicResponses extends XmlHttpResponse {
  def delete = {
    response(
      <DeleteTopicResponse xmlns="http://sns.amazonaws.com/doc/2010-03-31/">
        <ResponseMetadata>
          <RequestId>
            {UUID.randomUUID}
          </RequestId>
        </ResponseMetadata>
      </DeleteTopicResponse>
    )
  }

  def create(arn: String): HttpResponse = {
    response(
      <CreateTopicResponse xmlns="http://sns.amazonaws.com/doc/2010-03-31/">
        <CreateTopicResult>
          <TopicArn>
            {arn}
          </TopicArn>
        </CreateTopicResult>
        <ResponseMetadata>
          <RequestId>
            {UUID.randomUUID}
          </RequestId>
        </ResponseMetadata>
      </CreateTopicResponse>
    )
  }

  def list(arns: Iterable[String]): HttpResponse = {
    response(
      <ListTopicsResponse xmlns="http://sns.amazonaws.com/doc/2010-03-31/">
        <ListTopicsResult>
          <Topics>
            {arns.map(arn =>
            <member>
              <TopicArn>
                {arn}
              </TopicArn>
            </member>
          )}
          </Topics>
        </ListTopicsResult>
        <ResponseMetadata>
          <RequestId>
            {UUID.randomUUID}
          </RequestId>
        </ResponseMetadata>
      </ListTopicsResponse>
    )
  }
}

object TopicActor {
  def props = Props[TopicActor]
}

class TopicActor extends Actor {
  import TopicApi._
  var topics = mutable.HashMap.empty[String, Topic]

  private def findOrCreateTopic(name: String): Topic = {
    topics.values.find(_.name == name) match {
      case Some(topic) => topic
      case None =>
        val topic = Topic(s"arn:aws:sns:us-east-1:${System.currentTimeMillis}:$name", name)
        topics += topic.arn -> topic
        topic
    }
  }
  
  private def create(name: String): HttpResponse = TopicResponses.create(findOrCreateTopic(name).arn)
  
  private def delete(arn: String): HttpResponse = {
    if (topics.isDefinedAt(arn)) {
      topics -= arn
      TopicResponses.delete
    } else {
      HttpResponse(404, entity = "NotFound")
    }
  }

  private def list(): HttpResponse = TopicResponses.list(topics.keys)

  override def receive = {
    case CmdCreate(name) => sender ! create(name)
    case CmdDelete(arn) => sender ! delete(arn)
    case CmdList => sender ! list()
    case _ => sender ! HttpResponse(500, entity = "Invalid message")
  }
}
