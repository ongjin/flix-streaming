mvn clean package
docker build -t flix-streaming .
docker run -p 8080:8080 flix-streaming
