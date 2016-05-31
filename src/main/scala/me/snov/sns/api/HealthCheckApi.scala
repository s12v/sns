package me.snov.sns.api

import akka.http.scaladsl.server.Directives._

object HealthCheckApi {
  val route =
    path("health") {
      get {
        complete("OK")
      }
    }
}
