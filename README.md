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
