mvn clean package
docker build -t streaming-service .
docker run -p 8080:8080 streaming-service
