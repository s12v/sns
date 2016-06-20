[![Build Status](https://travis-ci.org/s12v/sns.svg?branch=master)](https://travis-ci.org/s12v/sns)
[![codecov](https://codecov.io/gh/s12v/sns/branch/master/graph/badge.svg)](https://codecov.io/gh/s12v/sns)
# Fake SNS

Fake Amazon Simple Notification Service (SNS) for testing. Supports:
 - Create/List/Delete topics
 - Subscribe endpoint
 - Publish message
 - Subscription persistence
 - Integrations with (Fake-)SQS, File, HTTP, RabbitMQ, Slack

## Usage

### Docker

Based on the official `java:8-jre-alpine` image. Run it with the command:
```
docker run -d -p 9911:9911 s12v/sns
```

If you would like to keep the topic/subscription database in the current host folder:
```
docker run -d -p 9911:9911 -v "$PWD":/etc/sns s12v/sns
```

#### Using aws-cli

The image has aws-cli preinstalled. For example, create a topic:
```
docker exec <CONTAINER_ID> 'aws sns --endpoint-url http://localhost:9911 create-topic --name test1'
```

### Jar

Download the latest release from https://github.com/s12v/sns/releases and run:
```
DB_PATH=/tmp/db.json java -jar sns-0.1.0.jar
```
Requires Java8.

## Configuration

Configuration can be set via environment variables:
 - `DB_PATH` - path to subscription database file, default: `db.json`
 - `HTTP_INTERFACE` - interface to bind to, default: `0.0.0.0`
 - `HTTP_PORT` - tcp port, default: `9911`

## Supported integrations

 - Amazon SQS: `aws-sqs://queueName?amazonSQSEndpoint=...&accessKey=&secretKey=`
 - RabbitMQ: `rabbitmq://hostname[:port]/exchangeName[?options]`
 - HTTP: `http:hostName[:port][/resourceUri][?options]`
 - File: `file://tmp?fileName=sns1.txt`
 - Slack: `slack:@username?webhookUrl=https://hooks.slack.com/services/aaa/bbb/ccc`

See [camel documentation](http://camel.apache.org/components.html) for more details

### Example fake SQS integration:

Tested with [elasticmq](https://github.com/adamw/elasticmq).
See example/docker-compose.yml and example/config/db.json

```
docker run -d -p 9911:9911 -v "$PWD/example/config":/etc/sns s12v/sns
```

## Development

### Unit tests

`sbt test`

### Integration tests

It's tested with AWS Ruby and PHP SDKs. Start elasticmq for SQS integration tests with
```
docker run -d -p 9324:9324 s12v/elasticmq
```

#### Ruby SDK tests:
```
bundle install
ENDPOINT=http://localhost:9911 bundle exec cucumber
```

#### PHP SDK tests:
```
composer install
./bin/behat
```
