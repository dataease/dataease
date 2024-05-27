FROM registry.cn-qingdao.aliyuncs.com/dataease/alpine-openjdk21-jre

RUN mkdir -p /opt/apps/config /opt/dataease2.0/drivers/ /opt/dataease2.0/cache/ /opt/dataease2.0/data/map /opt/dataease2.0/data/static-resource/ /opt/dataease2.0/data/appearance/ /opt/dataease2.0/data/exportData/

ADD drivers/* /opt/dataease2.0/drivers/
ADD mapFiles/ /opt/dataease2.0/data/map/
ADD staticResource/ /opt/dataease2.0/data/static-resource/

WORKDIR /opt/apps

ADD core/core-backend/target/CoreApplication.jar /opt/apps/app.jar

ENV JAVA_APP_JAR=/opt/apps/app.jar
ENV RUNNING_PORT=8100
ENV JAVA_OPTIONS="-Dfile.encoding=utf-8 -Dloader.path=/opt/apps -Dspring.config.additional-location=/opt/apps/config/"

HEALTHCHECK --interval=15s --timeout=5s --retries=20 --start-period=30s CMD nc -zv 127.0.0.1 $RUNNING_PORT

CMD ["/deployments/run-java.sh"]
