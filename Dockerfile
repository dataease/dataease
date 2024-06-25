FROM registry.cn-qingdao.aliyuncs.com/dataease/fabric8-java-alpine-openjdk8-jre:edge-chromium-11

ARG IMAGE_TAG

RUN mkdir -p /opt/apps /opt/dataease/data/feature/full /opt/dataease/drivers /opt/dataease/plugins/default

ADD core/mapFiles/full/ /opt/dataease/data/feature/full/

ADD core/drivers/* /opt/dataease/drivers/

ADD plugins/default/ /opt/dataease/plugins/default/

ADD core/backend/target/backend-$IMAGE_TAG.jar /opt/apps

ENV JAVA_APP_JAR=/opt/apps/backend-$IMAGE_TAG.jar
ENV AB_OFF=true
ENV JAVA_OPTIONS=-Dfile.encoding=utf-8
ENV RUNNING_PORT=8081

HEALTHCHECK --interval=15s --timeout=5s --retries=20 --start-period=30s CMD nc -zv 127.0.0.1 $RUNNING_PORT

CMD ["/deployments/run-java.sh"]
