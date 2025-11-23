FROM eclipse-temurin:17-jdk-jammy
WORKDIR /app
COPY target/clickhouse-encoder.jar /app/clickhouse-encoder.jar
CMD ["java", "-jar", "/app/clickhouse-encoder.jar"]