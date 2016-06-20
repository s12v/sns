FROM java:8-jre-alpine
MAINTAINER Sergey Novikov <snov@snov.me>

EXPOSE 9911

VOLUME /etc/sns

ENV AWS_DEFAULT_REGION=eu-west-1 \
	AWS_ACCESS_KEY_ID=foo \
	AWS_SECRET_ACCESS_KEY=bar \
	DB_PATH=/etc/sns/db.json \
	VERSION=0.1.0

# aws-cli
RUN apk -Uuv add python py-pip && \
	pip install awscli && \
	apk --purge -v del py-pip && \
	rm /var/cache/apk/*

ADD https://github.com/s12v/sns/releases/download/$VERSION/sns-$VERSION.jar /sns.jar

CMD ["java", "-jar", "/sns.jar"]
