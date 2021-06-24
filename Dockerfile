FROM registry.cn-qingdao.aliyuncs.com/dataease/fabric8-java-alpine-openjdk8-jre

ARG IMAGE_TAG

RUN mkdir -p /opt/apps

ADD backend/target/backend-1.0.0.jar /opt/apps

ENV JAVA_APP_JAR=/opt/apps/backend-1.0.0.jar

ENV AB_OFF=true

ENV JAVA_OPTIONS=-Dfile.encoding=utf-8

HEALTHCHECK --interval=15s --timeout=5s --retries=20 --start-period=30s CMD curl -f 127.0.0.1:8081

CMD ["/deployments/run-java.sh"]
