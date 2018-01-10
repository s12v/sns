package me.snov.sns.api

import java.util.concurrent.TimeUnit

import akka.actor.ActorRef
import akka.http.scaladsl.model.{FormData, StatusCodes}
import akka.http.scaladsl.testkit.ScalatestRouteTest
import akka.testkit.{TestActor, TestProbe}
import akka.util.Timeout
import me.snov.sns.actor.PublishActor.CmdPublish
import me.snov.sns.model.{Message, MessageAttribute}
import org.scalatest.{Matchers, WordSpec}

class PublishSpec extends WordSpec with Matchers with ScalatestRouteTest {
  implicit val timeout = new Timeout(100, TimeUnit.MILLISECONDS)

  val probe = TestProbe()
  val route = PublishApi.route(probe.ref)

  "Publish requires topic ARN" in {
    val params = Map("Action" -> "Publish")
    Post("/", FormData(params)) ~> route ~> check {
      status shouldBe StatusCodes.BadRequest
    }
  }

  "Sends publish command" in {
    val params = Map(
      "Action" -> "Publish",
      "TopicArn" -> "foo",
      "Message" -> "bar"
    )

    probe.setAutoPilot(new TestActor.AutoPilot {
      def run(sender: ActorRef, msg: Any) = {
        sender ! Message(Map("default" -> "foo"))
        this
      }
    })
    Post("/", FormData(params)) ~> route ~> check {
      probe.expectMsg(CmdPublish("foo", Map("default" -> "bar"), Map.empty))
    }
  }

  "Sends publish command with attributes" in {
    val params = Map(
      "Action" -> "Publish",
      "TopicArn" -> "foo",
      "Message" -> "bar",
      "MessageAttributes.entry.1.Value.DataType" -> "String",
      "MessageAttributes.entry.1.Value.StringValue" -> "AttributeValue",
      "MessageAttributes.entry.1.Name" -> "AttributeName"
    )

    probe.setAutoPilot(new TestActor.AutoPilot {
      def run(sender: ActorRef, msg: Any) = {
        sender ! Message(Map("default" -> "foo"), messageAttributes = Map("AttributeName" -> MessageAttribute("StringValue", "AttributeValue")))
        this
      }
    })
    Post("/", FormData(params)) ~> route ~> check {
      probe.expectMsg(CmdPublish("foo", Map("default" -> "bar"),Map("AttributeName" -> MessageAttribute("StringValue", "AttributeValue"))))
    }
  }
}
