FROM openjdk:17

EXPOSE 8761

ADD ./build/libs/*.jar app.jar

ENTRYPOINT ["java","-jar","/app.jar"]