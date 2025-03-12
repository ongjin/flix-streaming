# Base image: OpenJDK (버전에 맞게 수정)
FROM openjdk:17-jre-slim

# 작업 디렉토리 생성
WORKDIR /app

# Maven 빌드 산출물 복사 (jar 파일)
COPY target/flix-streaming-0.0.1-SNAPSHOT.jar app.jar

# 8080 포트 개방
EXPOSE 8080

# 애플리케이션 실행
ENTRYPOINT ["java", "-jar", "app.jar"]
