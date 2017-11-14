package me.snov.sns.response

import akka.http.scaladsl.model.{ContentTypes, HttpEntity, HttpResponse, StatusCode}

import scala.xml.Elem

trait XmlHttpResponse {

  def response(statusCode: StatusCode, xml: Elem) =
    HttpResponse(
      status = statusCode,
      entity = HttpEntity(ContentTypes.`text/xml(UTF-8)`, scala.xml.Utility.trim(xml).toString())
    )
}
