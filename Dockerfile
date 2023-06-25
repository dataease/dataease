FROM registry.cn-qingdao.aliyuncs.com/dataease/alpine-openjdk17-jre

ARG IMAGE_TAG

RUN mkdir -p /opt/apps/config

RUN mkdir -p /opt/dataease/drivers/
ADD drivers/* /opt/dataease/drivers/

WORKDIR /opt/apps

ADD core/core-backend/target/core-backend-$IMAGE_TAG.jar /opt/apps/app.jar

ENV JAVA_APP_JAR=/opt/apps/app.jar

HEALTHCHECK --interval=15s --timeout=5s --retries=20 --start-period=30s CMD curl -f 127.0.0.1:8100

#CMD ["/deployments/run-java.sh"]

CMD java -jar /opt/apps/app.jar -Xmx512m
