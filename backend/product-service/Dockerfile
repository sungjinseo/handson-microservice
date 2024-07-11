FROM openjdk:17-jdk-alpine3.14

EXPOSE 80

ADD ./build/libs/*.jar app.jar

ENTRYPOINT ["java", "-jar", "/app.jar"]