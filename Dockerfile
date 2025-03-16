# multi-stage build 예시
FROM openjdk:17-jdk-slim AS builder
WORKDIR /app
# 소스코드 복사 및 빌드
COPY . .
RUN ./mvnw clean package -DskipTests

FROM openjdk:17-jdk-slim
WORKDIR /app
# 빌드 결과물 복사 (예: target/*.jar)
COPY --from=builder /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
