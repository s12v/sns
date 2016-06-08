package me.snov.sns.util

import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer
import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

trait ToStrict {
  implicit val materializer: ActorMaterializer
  implicit val executor: ExecutionContext
  
  val toStrict = mapInnerRoute { innerRoute =>
    val timeout = 1.second
    extractRequest { req =>
      onSuccess(req.toStrict(timeout)) { strictReq =>
        mapRequest(_ => strictReq) {
          innerRoute
        }
      }
    }
  }
}
