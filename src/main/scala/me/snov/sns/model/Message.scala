package me.snov.sns.model

import spray.json._

import java.util.UUID

case class Message(body: String, uuid: UUID = UUID.randomUUID) {
}

object Message extends DefaultJsonProtocol {
  implicit object MessageJsonFormat extends RootJsonFormat[Message] {
    def write(msg: Message) = JsObject(
      "MessageId" -> JsString(msg.uuid.toString),
      "Message" -> JsString(msg.body),
      "Type" -> JsString("Notification")
    )

    def read(value: JsValue) = {
      value.asJsObject.getFields("MessageId", "Message", "Type") match {
        case Seq(JsString(uuid), JsString(body), _) => new Message(body, UUID.fromString(uuid))
        case _ => throw new DeserializationException("Message expected")
      }
    }
  }
}
