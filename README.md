# Streaming Service

Spring Boot 기반 스트리밍 서비스 예제 프로젝트입니다.

![용진 서버](https://github.com/user-attachments/assets/2d017b86-7749-457a-8a53-1718ce33c919)

## 주요 기능

-   비디오 스트리밍 처리 및 세션 관리
-   사용자 시청 기록 관리
-   콘텐츠 관리 및 조회
-   REST API (스트리밍 시작/종료, 세션 관리, 콘텐츠 조회)
-   PostgreSQL, Redis, filebeat, Kafka 연동
-   Docker 컨테이너화
-   Actuator를 통한 헬스체크 및 모니터링

## API 엔드포인트

### 스트리밍 관련 (StreamingController)

-   `GET /api/v1/stream/health` - 스트리밍 서비스 상태 확인
-   `GET /api/v1/stream/static/{filename}` - 정적 비디오 스트리밍
-   `GET /api/v1/stream/video/{videoId}` - 동적 비디오 스트리밍 (Range 요청 지원)

### 시청 기록 관련 (HistoryController)

-   `POST /api/v1/history/start` - 스트리밍 세션 시작
-   `GET /api/v1/history/resume` - 이어보기 기능 (마지막 재생 위치 조회)
-   `POST /api/v1/history/position` - 재생 위치 업데이트

### 콘텐츠 관련 (ContentController)

-   `GET /api/v1/content` - 콘텐츠 목록 조회
-   `GET /api/v1/content/{id}` - 특정 콘텐츠 상세 정보 조회

### 헬스체크

-   `GET /actuator/health` - 서비스 상태 확인
-   `GET /actuator/health/{component}` - 특정 컴포넌트 상태 확인

## 빌드 및 실행

1. 프로젝트 빌드:

    ```bash
    mvn clean package
    ```

2. Docker 이미지 빌드:

    ```bash
    docker build -t flix-streaming .
    ```

3. Docker Compose로 실행:
    ```bash
    docker-compose up -d
    ```

## 사용 기술

### Language

-   Java 17

### Framework

-   Spring Boot 3.4.3
-   Spring Data JPA
-   Spring Security
-   Spring Boot Actuator

### Database

-   PostgreSQL
-   Redis

### Infra/DevOps

-   GCP
-   Docker
-   Jenkins
-   Github Webhook

### Monitoring & Logging

-   ELK Stack
    -   Elasticsearch: 로그 및 이벤트 데이터 저장
    -   Logstash: 로그 수집 및 전처리
    -   Kibana: 시각화 및 모니터링 대시보드
-   Filebeat: 로그 수집기
-   Kafka: 이벤트 스트리밍
-   Prometheus (계획중)
-   Grafana (계획중)

### SCM

-   Git

## 프로젝트 구조

```
flix-streaming/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/zerry/streaming/
│   │   │       ├── controller/
│   │   │       │   ├── StreamingController.java
│   │   │       │   ├── HistoryController.java
│   │   │       │   └── ContentController.java
│   │   │       ├── service/
│   │   │       ├── repository/
│   │   │       ├── dto/
│   │   │       ├── entity/
│   │   │       └── util/
│   │   └── resources/
│   │       └── application.yml
│   └── test/
├── Dockerfile
├── docker-compose.yml
└── pom.xml
```
