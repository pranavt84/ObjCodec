FROM openjdk:17-jdk-slim
WORKDIR /app
COPY target/clickhouse-encoder.jar /app/clickhouse-encoder.jar
EXPOSE 8080
RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*
HEALTHCHECK --interval=30s --timeout=3s --start-period=5s --retries=3 \
  CMD curl -f http://localhost:8080/health || exit 1
ENTRYPOINT ["java", "-jar", "clickhouse-encoder.jar"]
CMD ["8080"]