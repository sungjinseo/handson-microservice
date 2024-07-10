FROM openjdk:17-jdk-alpine3.14

ARG ARG_PINPOINT_VER
ARG ARG_COLLECTOR_IP
ARG ARG_AGENT_ID
ARG ARG_APP_NAME
ARG ARG_ACTIVE
ENV PINPOINT_VER ${ARG_PINPOINT_VER}
ENV COLLECTOR_IP ${ARG_COLLECTOR_IP}
ENV AGENT_ID ${ARG_AGENT_ID}
ENV APP_NAME ${ARG_APP_NAME}
ENV ACTIVE ${ARG_ACTIVE}

ADD ./build/libs/*.jar app.jar
ADD https://github.com/pinpoint-apm/pinpoint/releases/download/v${PINPOINT_VER}/pinpoint-agent-${PINPOINT_VER}.tar.gz /usr/local
RUN tar -zxvf /usr/local/pinpoint-agent-${PINPOINT_VER}.tar.gz -C /usr/local

# Update the Pinpoint configuration
RUN sed -i 's/profiler.transport.grpc.collector.ip=127.0.0.1/profiler.transport.grpc.collector.ip=${COLLECTOR_IP}/g' /usr/local/pinpoint-agent-${PINPOINT_VER}/pinpoint-root.config
RUN sed -i 's/profiler.collector.ip=127.0.0.1/profiler.collector.ip=${COLLECTOR_IP}/g' /usr/local/pinpoint-agent-${PINPOINT_VER}/pinpoint-root.config

EXPOSE 8080


RUN echo $'java -jar \
  -javaagent:/usr/local/pinpoint-agent-${PINPOINT_VER}/pinpoint-bootstrap-${PINPOINT_VER}.jar \
  -Dpinpoint.config=/usr/local/pinpoint-agent-${PINPOINT_VER}/pinpoint-root.config \
  -Dpinpoint.agentId=${AGENT_ID} \
  -Dpinpoint.applicationName=${APP_NAME} \
  -Dspring.profiles.active=${ACTIVE} \
   /app.jar' \ >> /docker_entrypoint.sh

ENTRYPOINT ["sh", "/docker_entrypoint.sh"]