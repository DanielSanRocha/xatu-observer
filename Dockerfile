FROM adoptopenjdk/openjdk11:x86_64-ubuntu-jdk-11.0.18_10-slim

RUN mkdir -p /app
WORKDIR /app

COPY start.sh /app/start.sh
RUN chmod +x /app/start.sh

COPY xatu.jar /app/xatu.jar

ENTRYPOINT ["/app/start.sh", "/app/xatu.jar"]
