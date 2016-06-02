package me.snov.sns.service

import java.nio.charset.StandardCharsets
import java.nio.file.{StandardOpenOption, Paths, Files}

import spray.json.{JsValue, JsObject}

class DbService(dbFilePath: String) {

  val path = Paths.get(dbFilePath)
  
//  if (Files.notExists(path) || Files.isWritable(path)) {
//    throw new RuntimeException(s"$dbFilePath is not writable")
//  }
  
  def save(subscriptions: JsValue, topics: JsValue) = {
    val configuration = new JsObject(Map(
      "subscriptions" -> subscriptions,
      "topics" -> topics
    ))
    write(configuration.prettyPrint)
  }

  private def write(contents: String) = {
    Files.write(path, contents.getBytes(StandardCharsets.UTF_8), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)
     
  }
}
