# Streaming Service

Spring Boot 기반 스트리밍 서비스 예제 프로젝트입니다.

## 주요 기능

-   REST API (예: 헬스체크)
-   PostgreSQL, Redis, Kafka 연동
-   Docker 컨테이너화

## 빌드 및 실행

1.  프로젝트 빌드:
    ```bash
    mvn clean package
    docker build -t flix-streaming .
    docker run -p 8050:8050 flix-streaming
    ```

## 로그 포맷 예시

```
	{
	"timestamp": "2025-03-13T12:34:56.789Z",
	"service_name": "auth",
	"log_level": "INFO",
	"message": "사용자 로그인 성공",
	"request_id": "123e4567-e89b-12d3-a456-426614174000",
	"host": "auth-server-01",
	"environment": "production"
	}
주요 필드 설명:
timestamp: ISO 8601 형식의 시간 정보
service_name: 서비스 식별 (예: auth, streaming)
log_level: 로그 심각도 (INFO, WARN, ERROR 등)
message: 실제 로그 메시지
request_id: 분산 추적을 위한 고유 식별자
host: 서버 호스트 정보
environment: 배포 환경 정보
```

## 사용 기술

-   Language

```
	Java
```

-   Framework

```
	Spring boot 3.4.3
```

-   Infra/DevOps

```
	GCP, Docker, Jenkins, Github Webhook
```

-   SCM

```
	Git
```

-   기타

```
	Nginx, Kafka, redis, ELK(Elasticsearch: 로그 및 이벤트 데이터 저장, Logstash: 로그 수집 및 전처리, Kibana: 시각화 및 모니터링 대시보드 제공)
	Prometheus(사용고려중), Grafana(사용고려중)
```
