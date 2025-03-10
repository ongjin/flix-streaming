package com.zerry.streaming_service.Controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.zerry.streaming_service.Service.KafkaSender;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
@Slf4j
public class StreamingController {
    @Autowired
    private KafkaSender kafkaSender;

    @GetMapping("/health")
    public String healthCheck() {
        return "Streaming Service is running.";
    }

    @GetMapping("/")
    public String index() {

        log.trace("TRACE!!");
        log.debug("DEBUG!!");
        log.info("INFO!!");
        log.warn("WARN!!");
        log.error("ERROR!!");

        return "index";
    }

    /**
     * "test-topic" 토픽의 메시지를 수신합니다.
     * 
     * @param message 수신된 메시지
     */
    @KafkaListener(topics = "test-topic")
    public void listen(String message) {
        log.debug("Received message: {}", message);
    }

    @PostMapping("/kafka-send")
    public String kafkaSend(@RequestBody Map<String, Object> map) {
        kafkaSender.send(map.get("topic").toString(), map.get("message").toString());
        return null;
    }

}
