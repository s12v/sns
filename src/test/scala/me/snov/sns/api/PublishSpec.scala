package me.snov.sns.api

import java.util.concurrent.TimeUnit

import akka.actor.ActorRef
import akka.http.scaladsl.model.{FormData, HttpResponse, StatusCodes}
import akka.http.scaladsl.testkit.ScalatestRouteTest
import akka.testkit.{TestActor, TestProbe}
import akka.util.Timeout
import me.snov.sns.api.PublishActor.CmdPublish
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

  "Sends subscribe command" in {
    val params = Map(
      "Action" -> "Publish",
      "TopicArn" -> "foo",
      "Message" -> "bar"
    )

    probe.setAutoPilot(new TestActor.AutoPilot {
      def run(sender: ActorRef, msg: Any) = {
        sender ! HttpResponse(200)
        this
      }
    })
    Post("/", FormData(params)) ~> route ~> check {
      probe.expectMsg(CmdPublish("foo", "bar"))
    }
  }
}
