package me.snov.sns.util

import com.typesafe.config.ConfigFactory

trait Config {
  val config = ConfigFactory.load()
}
