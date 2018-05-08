package me.snov.sns.api

import akka.actor.ActorRef
import akka.event.Logging

import akka.http.scaladsl.model.HttpResponse
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.pattern.ask
import akka.util.Timeout
import me.snov.sns.actor.PublishActor.CmdPublish
import me.snov.sns.model.{Message, MessageAttribute, TopicNotFoundException}
import me.snov.sns.response.PublishResponse
import spray.json.DefaultJsonProtocol._
import spray.json._

import scala.concurrent.{ExecutionContext, Future}

case class InvalidTopicArnException(msg: String) extends Exception(msg)

object PublishApi {
  private val arnPattern = """([\w+_:-]{1,512})""".r

  def route(actorRef: ActorRef)(implicit timeout: Timeout, ec: ExecutionContext): Route = {
    pathSingleSlash {
      formField('Action ! "Publish") {
        formFieldSeq { fields =>
          val messageAttributes: Map[String, MessageAttribute] = MessageAttribute.parse(fields)
          formFields('TopicArn.?, 'TargetArn.?, 'MessageStructure.?, 'Message) { (topicArnMaybe, targetArnMaybe, messageStructure, message) =>
            try {
              topicArn(topicArnMaybe, targetArnMaybe) match {
                case arnPattern(topic) => complete {
                  val bodies = messageStructure match {
                    case Some("json") => message.parseJson.asJsObject.convertTo[Map[String, String]]
                    case Some(_) => throw new RuntimeException("Invalid MessageStructure value");
                    case None => Map("default" -> message)
                  }
                  (actorRef ? CmdPublish(topic, bodies, messageAttributes)).collect {
                    case m: Message => PublishResponse.publish(m)
                  }.recover {
                    case t: TopicNotFoundException => PublishResponse.topicNotFound(t.getMessage)
                    case t: Throwable => HttpResponse(500, entity = t.getMessage)
                  }
                }
                case _ => complete(HttpResponse(400, entity = "Invalid topic ARN"))
              }
            } catch {
              case e: InvalidTopicArnException => complete(HttpResponse(400, entity = e.getMessage))
              case e: RuntimeException => complete(HttpResponse(400, entity = e.getMessage))
            }
          }
        } ~
          complete(HttpResponse(400, entity = "TopicArn is required"))
      }
    }
  }

  private def topicArn(topicArnMaybe: Option[String], targetArnMaybe: Option[String]): String = {
    topicArnMaybe.getOrElse(targetArnMaybe.getOrElse(throw InvalidTopicArnException("Neither TopicArn nor TargetArn provided")))
  }
}

