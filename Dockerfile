FROM eclipse-temurin:21-jdk

ARG JAR_FILE=build/libs/*.jar

COPY ${JAR_FILE} app.jar
COPY infra/oom-notify.sh /oom-notify.sh
RUN chmod +x /oom-notify.sh

EXPOSE 8080

ENTRYPOINT ["java", "-Xmx128m", "-Xms64m", "-jar", "/app.jar"]