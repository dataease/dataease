FROM openjdk:17-oracle

ARG IMAGE_TAG

RUN mkdir -p /opt/apps

ADD core/core-backend/target/core-backend-$IMAGE_TAG.jar /opt/apps/app.jar

RUN mkdir -p /opt/apps/config

#ADD core/core-backend/src/main/resources/application-standalone.yml /opt/apps/config/application.yml

EXPOSE 8100

CMD java -jar /opt/apps/app.jar -Xmx512m

#ENTRYPOINT ["java", "-jar","app.jar"]
