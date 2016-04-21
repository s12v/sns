package me.snov.sns.model

import java.util.UUID

case class Message(message: String) {
  val uuid = UUID.randomUUID()
}
