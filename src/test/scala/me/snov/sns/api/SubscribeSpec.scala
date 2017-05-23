package me.snov.sns.api

import java.util.concurrent.TimeUnit

import akka.actor.ActorRef
import akka.http.scaladsl.model.{FormData, HttpResponse, StatusCodes}
import akka.http.scaladsl.testkit.ScalatestRouteTest
import akka.testkit.{TestActor, TestProbe}
import akka.util.Timeout
import me.snov.sns.actor.SubscribeActor.{CmdListSubscriptionsByTopic, CmdListSubscriptions, CmdSubscribe, CmdUnsubscribe}
import me.snov.sns.model.Subscription
import org.scalatest.{Matchers, WordSpec}

class SubscribeSpec extends WordSpec with Matchers with ScalatestRouteTest {
  implicit val timeout = new Timeout(100, TimeUnit.MILLISECONDS)

  val probe = TestProbe()
  val route = SubscribeApi.route(probe.ref)

  "Subscribe requires topic ARN" in {
    val params = Map("Action" -> "Subscribe", "Endpoint" -> "aaa", "Protocol" -> "bbb")
    Post("/", FormData(params)) ~> route ~> check {
      status shouldBe StatusCodes.BadRequest
    }
  }

  "Subscribe requires endpoint" in {
    val params = Map("Action" -> "Subscribe", "TopicArn" -> "aaa", "Protocol" -> "ccc")
    Post("/", FormData(params)) ~> route ~> check {
      status shouldBe StatusCodes.BadRequest
    }
  }

  "Subscribe requires protocol" in {
    val params = Map("Action" -> "Subscribe", "TopicArn" -> "aaa", "Endpoint" -> "bbb")
    Post("/", FormData(params)) ~> route ~> check {
      status shouldBe StatusCodes.BadRequest
    }
  }

  "Sends subscribe command" in {
    val params = Map(
      "Action" -> "Subscribe",
      "TopicArn" -> "aaa",
      "Protocol" -> "bbb",
      "Endpoint" -> "ccc"
    )

    probe.setAutoPilot(new TestActor.AutoPilot {
      def run(sender: ActorRef, msg: Any) = {
        sender ! Subscription("foo", "bar", "aaa", "bbb", "ccc")
        this
      }
    })
    Post("/", FormData(params)) ~> route ~> check {
      probe.expectMsg(CmdSubscribe("aaa", "bbb", "ccc"))
    }
  }

  "Sends CmdList" in {
    val params = Map("Action" -> "ListSubscriptions")
    probe.setAutoPilot(new TestActor.AutoPilot {
      def run(sender: ActorRef, msg: Any) = {
        sender ! List()
        this
      }
    })
    Post("/", FormData(params)) ~> route ~> check {
      probe.expectMsg(CmdListSubscriptions())
    }
  }

  "Sends CmdListByTopic" in {
    val params = Map("Action" -> "ListSubscriptionsByTopic", "TopicArn" -> "foo")
    probe.setAutoPilot(new TestActor.AutoPilot {
      def run(sender: ActorRef, msg: Any) = {
        sender ! List()
        this
      }
    })
    Post("/", FormData(params)) ~> route ~> check {
      probe.expectMsg(CmdListSubscriptionsByTopic("foo"))
    }
  }

  "Unsubscribe requires SubscriptionArn" in {
    val params = Map("Action" -> "Unsubscribe")
    Post("/", FormData(params)) ~> route ~> check {
      status shouldBe StatusCodes.BadRequest
    }
  }

  "Sends unsubscribe command" in {
    val params = Map(
      "Action" -> "Unsubscribe",
      "SubscriptionArn" -> "foo"
    )

    probe.setAutoPilot(new TestActor.AutoPilot {
      def run(sender: ActorRef, msg: Any) = {
        sender ! Subscription("foo", "bar", "aaa", "bbb", "ccc")
        this
      }
    })
    Post("/", FormData(params)) ~> route ~> check {
      probe.expectMsg(CmdUnsubscribe("foo"))
    }
  }
}
