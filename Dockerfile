FROM registry.cn-qingdao.aliyuncs.com/dataease/fabric8-java-alpine-openjdk8-jre

ARG IMAGE_TAG

RUN sed -i 's/dl-cdn.alpinelinux.org/mirrors.aliyun.com/g' /etc/apk/repositories

RUN apk add chromium=77.0.3865.120-r0 --no-cache

RUN apk add chromium-chromedriver=77.0.3865.120-r0 --no-cache

RUN mkdir -p /opt/apps

RUN mkdir -p /opt/dataease/data/feature/full

ADD mapFiles/* /opt/dataease/data/feature/full/

RUN mkdir -p /opt/dataease/drivers

ADD drivers/* /opt/dataease/drivers/

ADD backend/target/backend-$IMAGE_TAG.jar /opt/apps

ENV JAVA_APP_JAR=/opt/apps/backend-$IMAGE_TAG.jar

ENV AB_OFF=true

ENV JAVA_OPTIONS=-Dfile.encoding=utf-8

HEALTHCHECK --interval=15s --timeout=5s --retries=20 --start-period=30s CMD curl -f 127.0.0.1:8081

CMD ["/deployments/run-java.sh"]
