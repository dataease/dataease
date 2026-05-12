FROM registry.cn-qingdao.aliyuncs.com/dataease/alpine-openjdk21-jre
STOPSIGNAL SIGTERM
RUN mkdir -p /opt/dataease2.0/drivers/ \
    /opt/dataease2.0/data/map-origin/ \
    /opt/dataease2.0/data/static-resource/

ADD drivers/* /opt/dataease2.0/drivers/
ADD staticResource/ /opt/dataease2.0/data/static-resource/
COPY mapFiles /opt/dataease2.0/data/map-origin

WORKDIR /opt/apps

ADD core/core-backend/target/CoreApplication.jar /opt/apps/app.jar

ENV JAVA_APP_JAR=/opt/apps/app.jar
ENV RUNNING_PORT=8100
ENV JAVA_OPTIONS="-Dfile.encoding=utf-8 -Dloader.path=/opt/apps -Dspring.config.additional-location=/opt/apps/config/"

HEALTHCHECK --interval=15s --timeout=5s --retries=20 --start-period=30s CMD nc -zv 127.0.0.1 $RUNNING_PORT

CMD ["/deployments/run-java.sh"]
