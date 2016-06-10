package me.snov.sns.api

import java.util.concurrent.TimeUnit

import akka.actor.ActorRef
import akka.http.scaladsl.model.{FormData, HttpResponse, StatusCodes}
import akka.http.scaladsl.testkit.ScalatestRouteTest
import akka.testkit.{TestActor, TestProbe}
import akka.util.Timeout
import me.snov.sns.actor.SubscribeActor.{CmdDeleteTopic, CmdCreateTopic}
import me.snov.sns.model.Topic
import org.scalatest.{Matchers, WordSpec}

class TopicSpec extends WordSpec with Matchers with ScalatestRouteTest {
  implicit val timeout = new Timeout(100, TimeUnit.MILLISECONDS)

  val probe = TestProbe()
  val route = TopicApi.route(probe.ref)

  "Requires topic name" in {
    Post("/", FormData(Map("Action" -> "CreateTopic"))) ~> route ~> check {
      status shouldBe StatusCodes.BadRequest
    }
  }

  "Validates topic name" in {
    Post("/", FormData(Map("Action" -> "CreateTopic", "Name" -> "f$$"))) ~> route ~> check {
      status shouldBe StatusCodes.BadRequest
    }
  }

  "TopicDelete validates topic name" in {
    Post("/", FormData(Map("Action" -> "DeleteTopic", "TopicArn" -> "f$$"))) ~> route ~> check {
      status shouldBe StatusCodes.BadRequest
    }
  }

  "Sends create command to actor" in {
    probe.setAutoPilot(new TestActor.AutoPilot {
      def run(sender: ActorRef, msg: Any) = {
        sender ! new Topic("foo", "bar")
        this
      }
    })
    Post("/", FormData(Map("Action" -> "CreateTopic", "Name" -> "foo"))) ~> route ~> check {
      probe.expectMsg(CmdCreateTopic("foo"))
    }
  }

  "Sends delete command to actor" in {
    probe.setAutoPilot(new TestActor.AutoPilot {
      def run(sender: ActorRef, msg: Any) = {
        sender ! new Topic("foo", "bar")
        this
      }
    })
    Post("/", FormData(Map("Action" -> "DeleteTopic", "TopicArn" -> "arn-foo"))) ~> route ~> check {
      probe.expectMsg(CmdDeleteTopic("arn-foo"))
    }
  }
}
