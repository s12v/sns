package me.snov.sns.response

import akka.http.scaladsl.model.{ContentTypes, HttpEntity, HttpResponse}

import scala.xml.Elem

trait XmlHttpResponse {

  def response(xml: Elem) =
    HttpResponse(
      entity = HttpEntity(ContentTypes.`text/xml(UTF-8)`, scala.xml.Utility.trim(xml).toString())
    )
}
