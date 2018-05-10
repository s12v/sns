name := "sns"

version := "0.4.1"

scalaVersion := "2.12.4"

// sbt-assembly
assemblyJarName in assembly := s"sns-${version.value}.jar"
test in assembly := {}

val akkaVersion = "2.5.6"
val akkaHttpVersion = "10.0.10"
val camelVersion = "2.19.4"

libraryDependencies ++= {
  Seq(
    "com.typesafe.akka" %% "akka-actor" % akkaVersion,
    "com.typesafe.akka" %% "akka-stream" % akkaVersion,

    "com.typesafe.akka" %% "akka-http" % akkaHttpVersion,
    "com.typesafe.akka" %% "akka-http-xml" % akkaHttpVersion,
    "com.typesafe.akka" %% "akka-http-spray-json" % akkaHttpVersion,
    "com.typesafe.akka" %% "akka-http-testkit" % akkaHttpVersion % Test,

    "org.slf4j" % "slf4j-api" % "1.7.2",
    "ch.qos.logback" % "logback-classic" % "1.0.7",
    "com.typesafe.akka" %% "akka-camel" % akkaVersion,
    "com.amazonaws" % "aws-java-sdk-sqs" % "1.11.228",
    "org.apache.camel" % "camel-aws" % camelVersion
      excludeAll ExclusionRule(organization = "com.amazonaws")
    ,
    "org.apache.camel" % "camel-http" % camelVersion,
    "org.apache.camel" % "camel-rabbitmq" % camelVersion,
    "org.apache.camel" % "camel-slack" % camelVersion
      exclude("junit", "junit")
    ,
    "org.scalatest" %% "scalatest" % "3.0.4" % Test
  )
}

dependencyOverrides += "com.typesafe.akka" %% "akka-actor" % akkaVersion
dependencyOverrides += "com.typesafe.akka" %% "akka-stream" % akkaVersion
