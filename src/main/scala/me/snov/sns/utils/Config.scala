package me.snov.sns.utils

import com.typesafe.config.ConfigFactory

trait Config {
  val config = ConfigFactory.load()
}
