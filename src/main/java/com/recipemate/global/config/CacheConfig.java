package com.recipemate.global.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * Redis 캐싱 설정
 * 
 * 캐시 이름별 TTL:
 * - recipes: 1시간 (외부 API 데이터, 자주 변경되지 않음)
 * - popularGroupBuys: 5분 (자주 조회되는 인기 공구 목록)
 * - viewCounts: 1분 (조회수는 실시간성이 중요하지 않음)
 */
@Configuration
@EnableCaching
public class CacheConfig {
    
    public static final String RECIPES_CACHE = "recipes";
    public static final String POPULAR_GROUP_BUYS_CACHE = "popularGroupBuys";
    public static final String VIEW_COUNTS_CACHE = "viewCounts";
    
    @Bean
    @ConditionalOnProperty(name = "spring.cache.type", havingValue = "redis")
    public CacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        // 기본 캐시 설정 (1시간 TTL)
        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofHours(1))
                .serializeKeysWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer())
                )
                .serializeValuesWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer())
                )
                .disableCachingNullValues();
        
        // 캐시별 개별 TTL 설정
        Map<String, RedisCacheConfiguration> cacheConfigurations = new HashMap<>();
        
        // 레시피 캐시: 1시간
        cacheConfigurations.put(RECIPES_CACHE, defaultConfig.entryTtl(Duration.ofHours(1)));
        
        // 인기 공구 목록 캐시: 5분
        cacheConfigurations.put(POPULAR_GROUP_BUYS_CACHE, defaultConfig.entryTtl(Duration.ofMinutes(5)));
        
        // 조회수 캐시: 1분
        cacheConfigurations.put(VIEW_COUNTS_CACHE, defaultConfig.entryTtl(Duration.ofMinutes(1)));
        
        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(defaultConfig)
                .withInitialCacheConfigurations(cacheConfigurations)
                .build();
    }
}
