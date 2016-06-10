name := "sns"

version := "0.0.2"

scalaVersion := "2.11.8"

assemblyJarName in assembly := s"sns-${version.value}.jar"

libraryDependencies ++= {
  val akkaVersion = "2.4.6"

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
    "org.apache.camel" % "camel-aws" % "2.17.0"
      exclude("com.amazonaws", "aws-java-sdk-acm")
      exclude("com.amazonaws", "aws-java-sdk-api-gateway")
      exclude("com.amazonaws", "aws-java-sdk-autoscaling")
      exclude("com.amazonaws", "aws-java-sdk-cloudformation")
      exclude("com.amazonaws", "aws-java-sdk-cloudfront")
      exclude("com.amazonaws", "aws-java-sdk-cloudwatch")
      exclude("com.amazonaws", "aws-java-sdk-cloudwatchmetrics")
      exclude("com.amazonaws", "aws-java-sdk-codedeploy")
      exclude("com.amazonaws", "aws-java-sdk-codecommit")
      exclude("com.amazonaws", "aws-java-sdk-codepipeline")
      exclude("com.amazonaws", "aws-java-sdk-cognitoidentity")
      exclude("com.amazonaws", "aws-java-sdk-cognitosync")
      exclude("com.amazonaws", "aws-java-sdk-datapipeline")
      exclude("com.amazonaws", "aws-java-sdk-directconnect")
      exclude("com.amazonaws", "aws-java-sdk-kinesis")
      exclude("com.amazonaws", "aws-java-sdk-opsworks")
      exclude("com.amazonaws", "aws-java-sdk-ses")
      exclude("com.amazonaws", "aws-java-sdk-cloudsearch")
      exclude("com.amazonaws", "aws-java-sdk-swf-libraries")
      exclude("com.amazonaws", "aws-java-sdk-lambda")
      exclude("com.amazonaws", "aws-java-sdk-ecs")
      exclude("com.amazonaws", "aws-java-sdk-workspaces")
      exclude("com.amazonaws", "aws-java-sdk-machinelearning")
      exclude("com.amazonaws", "aws-java-sdk-directory")
      exclude("com.amazonaws", "aws-java-sdk-efs")
      exclude("com.amazonaws", "aws-java-sdk-waf")
      exclude("com.amazonaws", "aws-java-sdk-marketplacecommerceanalytics")
      exclude("com.amazonaws", "aws-java-sdk-inspector")
      exclude("com.amazonaws", "aws-java-sdk-iot")
      exclude("com.amazonaws", "aws-java-sdk-gamelift")
      exclude("com.amazonaws", "aws-java-sdk-simpledb")
      exclude("com.amazonaws", "aws-java-sdk-simpleworkflow")
      exclude("com.amazonaws", "aws-java-sdk-storagegateway")
      exclude("com.amazonaws", "aws-java-sdk-s3")
      exclude("com.amazonaws", "aws-java-sdk-route53")
      exclude("com.amazonaws", "aws-java-sdk-kms")
      exclude("com.amazonaws", "aws-java-sdk-sts")
      exclude("com.amazonaws", "aws-java-sdk-rds")
      exclude("com.amazonaws", "aws-java-sdk-redshift")
      exclude("com.amazonaws", "aws-java-sdk-glacier")
      exclude("com.amazonaws", "aws-java-sdk-elasticloadbalancing")
      exclude("com.amazonaws", "aws-java-sdk-emr")
      exclude("com.amazonaws", "aws-java-sdk-ec2")
      exclude("com.amazonaws", "aws-java-sdk-elasticache")
      exclude("com.amazonaws", "aws-java-sdk-dynamodb")
      exclude("com.amazonaws", "aws-java-sdk-iam")
      exclude("com.amazonaws", "aws-java-sdk-cloudtrail")
      exclude("com.amazonaws", "aws-java-sdk-elastictranscoder")
      exclude("com.amazonaws", "aws-java-sdk-elasticsearch")
      exclude("com.amazonaws", "aws-java-sdk-ssm")
      exclude("com.amazonaws", "aws-java-sdk-cloudhsm")
      exclude("com.amazonaws", "aws-java-sdk-devicefarm")
      exclude("com.amazonaws", "aws-java-sdk-elasticbeanstalk")
      exclude("com.amazonaws", "aws-java-sdk-ecr")
    ,
    "org.apache.camel" % "camel-http" % "2.17.0",
    "org.apache.camel" % "camel-rabbitmq" % "2.17.0",
    "org.apache.camel" % "camel-slack" % "2.17.0",
    
    "org.scalatest"     %% "scalatest" % "2.2.6" % Test
  )
}
