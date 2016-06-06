package me.snov.sns.service

import java.nio.charset.StandardCharsets
import java.nio.file.{Files, Paths, StandardOpenOption}

import akka.event.LoggingAdapter
import me.snov.sns.model.Configuration
import spray.json._

class DbService(dbFilePath: String)(implicit log: LoggingAdapter) {

  val subscriptionsName = "subscriptions"
  val topicsName = "topics"
  
  val path = Paths.get(dbFilePath)
  
  def load(): Option[Configuration] = {
    if (Files.exists(path)) {
      log.debug("Loading DB")
      try {
        val configuration = read().parseJson.convertTo[Configuration]
        log.info("Loaded DB")
        return Some(configuration)
      } catch {
        case e: DeserializationException => log.warning("Unable to parse configuration")
        case e: RuntimeException => log.warning("Unable to load configuration")
      }
    }
    None
  }
  
  def save(configuration: Configuration) = {
    log.debug("Saving DB")
    write(configuration.toJson.prettyPrint)
  }

  private def write(contents: String) = {
    Files.write(path, contents.getBytes(StandardCharsets.UTF_8), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)
  }

  private def read(): String = {
    new String(Files.readAllBytes(path))
  }
}
