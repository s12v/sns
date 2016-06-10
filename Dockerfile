FROM java:8-jre-alpine
MAINTAINER Sergey Novikov <snov@snov.me>

EXPOSE 9911

VOLUME /etc/sns

ENV DB_PATH=/etc/sns/db.json VERSION=0.0.3

ADD https://github.com/s12v/sns/releases/download/$VERSION/sns-$VERSION.jar /sns.jar

CMD ["java", "-jar", "/sns.jar"]
