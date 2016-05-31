package me.snov.sns.api

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.testkit.ScalatestRouteTest
import org.scalatest.{Matchers, WordSpec}

class HealthCheckSpec extends WordSpec with Matchers with ScalatestRouteTest {
  "Health check should return OK" in {
    Get("/health") ~> HealthCheckApi.route ~> check {
      status shouldBe StatusCodes.OK
      responseAs[String] shouldEqual "OK"
    }
  }
}
