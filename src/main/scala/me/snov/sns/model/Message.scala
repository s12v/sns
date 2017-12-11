package me.snov.sns.model

import spray.json._
import java.util.UUID

case class Message(bodies: Map[String, String], uuid: UUID = UUID.randomUUID, messageAttributes: Map[String, MessageAttribute] = Map.empty) {
}


object Message extends DefaultJsonProtocol {
  implicit object MessageJsonFormat extends RootJsonFormat[Message] {
    def write(msg: Message) = JsObject(
      "MessageId" -> JsString(msg.uuid.toString),
      "Message" -> JsString(msg.bodies.getOrElse("default", "")),
      "Type" -> JsString("Notification")
    )

    def read(value: JsValue) = {
      value.asJsObject.getFields("MessageId", "Message", "Type") match {
        case Seq(JsString(uuid), JsString(body), _) => new Message(Map("default" -> body), UUID.fromString(uuid))
        case _ => throw new DeserializationException("Message expected")
      }
    }
  }
}
//
case class MessageAttribute(
  dataType: String,
  stringValue: String
)

object MessageAttribute {
  /** Parse the AWS format for message atttributes in form fields.
    * MessageAttributes.entry.1.Value.DataType=String
    * MessageAttributes.entry.1.Value.StringValue=AttributeValue
    * MessageAttributes.entry.1.Name=AttributeName
    * MessageAttributes.entry.2.Value.DataType=String
    * MessageAttributes.entry.2.Value.StringValue=AttributeValue2
    * MessageAttributes.entry.2.Name=AttributeName2
    * @param fields
    */
  def parse(fields:Seq[(String,String)]): Map[String,MessageAttribute] = {
    val splitFields = fields.map(f => f._1.split("\\.").toList ++ List(f._2))

    val names = splitFields.collect {
        case "MessageAttributes"::"entry"::index::"Name"::value::x => (index -> value)
      }.toMap

    val values= splitFields.collect {
      case "MessageAttributes"::"entry"::index::"Value"::"StringValue"::value::x => (names(index) -> MessageAttribute("StringValue", value))
    }.toMap

    values

  }
}
