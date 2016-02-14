name := "SNS"

version := "0.0.1"

scalaVersion := "2.11.7"

libraryDependencies ++= {
  val akkaVersion = "2.4.2"

  Seq(
    "com.typesafe.akka" %% "akka-actor" % akkaVersion,
    "com.typesafe.akka" %% "akka-stream" % akkaVersion,
    "com.typesafe.akka" %% "akka-http-experimental" % akkaVersion,
    "com.typesafe.akka" %% "akka-http-xml-experimental" % akkaVersion,
    "com.typesafe.akka" %% "akka-http-testkit" % akkaVersion % Test,
    "org.scalatest"     %% "scalatest" % "2.2.6" % Test
  )
}
