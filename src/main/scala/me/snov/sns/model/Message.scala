package me.snov.sns.model

import java.util.UUID

case class Message(body: String) {
  val uuid = UUID.randomUUID()
}
