package me.snov.sns.model

import spray.json._

case class Topic(val arn: String, val name: String)

object Topic extends DefaultJsonProtocol {
  implicit val format = jsonFormat2(Topic.apply)
}
