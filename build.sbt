name := "sns"

version := "0.0.1"

scalaVersion := "2.11.8"

assemblyJarName in assembly := s"sns-${version.value}.jar"

libraryDependencies ++= {
  val akkaVersion = "2.4.4"

  Seq(
    "com.typesafe.akka" %% "akka-actor" % akkaVersion,
    "com.typesafe.akka" %% "akka-stream" % akkaVersion,
    "com.typesafe.akka" %% "akka-http-experimental" % akkaVersion,
    "com.typesafe.akka" %% "akka-http-xml-experimental" % akkaVersion,
    "com.typesafe.akka" %% "akka-http-spray-json-experimental" % akkaVersion,
    "com.typesafe.akka" %% "akka-http-testkit" % akkaVersion % Test,

    "org.slf4j" % "slf4j-api" % "1.7.2",
    "ch.qos.logback" % "logback-classic" % "1.0.7",
    "com.typesafe.akka" %% "akka-camel" % "2.3.15",
    "org.apache.camel" % "camel-aws" % "2.17.0",
    "org.apache.camel" % "camel-http" % "2.17.0",
    "org.apache.camel" % "camel-rabbitmq" % "2.17.0",
    "org.apache.camel" % "camel-slack" % "2.17.0",
    
    "org.scalatest"     %% "scalatest" % "2.2.6" % Test
  )
}
