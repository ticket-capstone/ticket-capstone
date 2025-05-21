package com.capstone.ticketservice.config;

import com.capstone.ticketservice.event.model.Event;
import com.capstone.ticketservice.seat.dto.PerformanceSeatDto;
import com.capstone.ticketservice.seat.model.PerformanceSeat;
import com.capstone.ticketservice.waitingqueue.model.WaitingQueue;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;

@Configuration
@EnableCaching
public class RedisConfig {

    @Bean(name = "redisQueueTemplate")
    public RedisTemplate<String, String> redisQueueTemplate(RedisConnectionFactory factory) {
        RedisTemplate<String, String> template = new RedisTemplate<>();
        template.setConnectionFactory(factory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new StringRedisSerializer());
        return template;
    }


    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return mapper;
    }

    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory connectionFactory, ObjectMapper objectMapper) {
        // Event 클래스를 위한 Jackson2JsonRedisSerializer 생성
        Jackson2JsonRedisSerializer<Event> eventSerializer = new Jackson2JsonRedisSerializer<>(Event.class);
        eventSerializer.setObjectMapper(objectMapper);

        // Event 리스트를 위한 Jackson2JsonRedisSerializer 생성
        Jackson2JsonRedisSerializer<Object> listSerializer = new Jackson2JsonRedisSerializer<>(Object.class);
        listSerializer.setObjectMapper(objectMapper);

        // PerformanceSeat 클래스를 위한 Jackson2JsonRedisSerializer 생성
        Jackson2JsonRedisSerializer<PerformanceSeat> seatSerializer = new Jackson2JsonRedisSerializer<>(PerformanceSeat.class);
        seatSerializer.setObjectMapper(objectMapper);

        // PerformanceSeatDto 클래스를 위한 Jackson2JsonRedisSerializer 생성
        Jackson2JsonRedisSerializer<PerformanceSeatDto> seatDtoSerializer = new Jackson2JsonRedisSerializer<>(PerformanceSeatDto.class);
        seatDtoSerializer.setObjectMapper(objectMapper);


        // 기본 캐시 설정
        RedisCacheConfiguration cacheConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(10))
                .serializeKeysWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(listSerializer));

        // 이벤트 단일 조회를 위한 특별 캐시 설정
        RedisCacheConfiguration eventCacheConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(10))
                .serializeKeysWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(eventSerializer));

        // 좌석 단일 조회를 위한 특별 캐시 설정
        RedisCacheConfiguration seatCacheConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofSeconds(30))  // 좌석 정보는 더 짧은 TTL로 설정 (30초)
                .serializeKeysWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(seatSerializer));

        // 좌석 목록 조회를 위한 특별 캐시 설정
        RedisCacheConfiguration seatDtoCacheConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofSeconds(30))  // 좌석 정보는 더 짧은 TTL로 설정 (30초)
                .serializeKeysWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(seatDtoSerializer));


        // 캐시 매니저 생성
        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(cacheConfig)
                .withCacheConfiguration("event", eventCacheConfig)  // "event" 캐시에 대한 특별 설정
                .withCacheConfiguration("performanceSeat", seatCacheConfig)  // "performanceSeat" 캐시 설정
                .withCacheConfiguration("performanceSeatDto", seatDtoCacheConfig)  // "performanceSeatDto" 캐시 설정
                .withCacheConfiguration("eventSeats", cacheConfig)  // "eventSeats" 캐시 설정 (좌석 목록)
                .build();
    }
}