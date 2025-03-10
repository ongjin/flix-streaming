package com.zerry.streaming_service.config;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 애플리케이션 설정 클래스
 *
 * 이 클래스는 Spring의 설정(Configuration) 클래스로,
 * 
 * @Bean 메서드를 정의하여 특정 객체를 Spring 컨테이너에 등록하는 역할을 합니다.
 *
 *       주요 역할:
 *       - Spring Bean 등록 및 관리
 *       - 애플리케이션의 설정 및 의존성 주입 관리
 *       - 자동 설정(Auto Configuration) 커스터마이징
 *       - 필요에 따라 보안, 데이터베이스, 웹 설정을 정의
 *
 * @Configuration 이 없으면 Spring이 설정 클래스로 인식하지 않음!
 */

@Configuration
@EnableKafka
public class KafkaConfig {

    // 공통 Kafka 브로커 주소
    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    // 인증 서비스 전용 컨슈머 그룹 아이디
    @Value("${spring.kafka.consumer.group-id.streaming-service}")
    private String authServiceGroupId;

    /**
     * Producer 설정: Kafka에 메시지 전송 시 필요한 프로퍼티들을 구성합니다.
     */
    @Bean
    public Map<String, Object> producerConfigs() {
        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        // 추가 Producer 설정 (예: acks, retries 등) 필요 시 추가
        return props;
    }

    /**
     * ProducerFactory 빈을 생성합니다.
     */
    @Bean
    public ProducerFactory<String, String> producerFactory() {
        return new DefaultKafkaProducerFactory<>(producerConfigs());
    }

    /**
     * KafkaTemplate을 생성하여 메시지 전송에 사용합니다.
     */
    @Bean
    public KafkaTemplate<String, String> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }

    /**
     * Consumer 설정: Kafka로부터 메시지 수신 시 필요한 프로퍼티들을 구성합니다.
     */
    @Bean
    public Map<String, Object> consumerConfigs() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        // 전용 그룹 아이디 설정
        props.put(ConsumerConfig.GROUP_ID_CONFIG, authServiceGroupId);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        // 추가 Consumer 설정 (예: auto.offset.reset 등) 필요 시 추가
        return props;
    }

    /**
     * ConsumerFactory 빈을 생성합니다.
     */
    @Bean
    public ConsumerFactory<String, String> consumerFactory() {
        return new DefaultKafkaConsumerFactory<>(consumerConfigs());
    }

    /**
     * KafkaListenerContainerFactory를 생성하여 @KafkaListener를 통한 메시지 수신을 가능하게 합니다.
     */
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, String> kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, String> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory());
        return factory;
    }
}
