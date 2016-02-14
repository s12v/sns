package me.snov.sns.api

import akka.http.scaladsl.server.Directives._

trait HealthCheck {
  val healthCheckRoutes =
    path("health") {
      get {
        complete("OK")
      }
    }
}
