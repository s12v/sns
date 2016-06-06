package me.snov.sns.model

import spray.json._

case class Configuration(
                          version: Int = 1,
                          timestamp: Long = System.currentTimeMillis(),
                          subscriptions: List[Subscription],
                          topics: List[Topic]
                        )

object Configuration extends DefaultJsonProtocol {
  implicit val format = jsonFormat4(Configuration.apply)
}
