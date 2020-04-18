package com.herokuapp.ddmura.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.GenericToStringSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import java.time.Duration;

@Configuration
@EnableTransactionManagement
public class RedisConfig {

    @Value("${cache.read-expire-time}")
    private int defaultExpireTime;

//    @Value("${cache.test.expire-time:180}")
//    private int testExpireTime;
//    @Value("${cache.test.name:test}")
//    private String testCacheName;


    //缓存管理器
    @Bean
    public CacheManager cacheManager(LettuceConnectionFactory lettuceConnectionFactory) {
        RedisCacheConfiguration defaultCacheConfig = RedisCacheConfiguration.defaultCacheConfig();
        // 设置缓存管理器管理的缓存的默认过期时间
        defaultCacheConfig = defaultCacheConfig.entryTtl(Duration.ofSeconds(defaultExpireTime))
                .computePrefixWith(cacheName -> cacheName + ":")
                // 设置 key为string序列化
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                // 设置value为json序列化
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer()));

//        Set<String> cacheNames = new HashSet<>();
//        cacheNames.add(testCacheName);
//
//        // 对每个缓存空间应用不同的配置
//        Map<String, RedisCacheConfiguration> configMap = new HashMap<>();
//        configMap.put(testCacheName, defaultCacheConfig.entryTtl(Duration.ofSeconds(testExpireTime)));

        return RedisCacheManager.builder(lettuceConnectionFactory)
                .cacheDefaults(defaultCacheConfig)
//                .initialCacheNames(cacheNames)
//                .withInitialCacheConfigurations(configMap)
                .build();
    }

    @Bean
    RedisTemplate<String, Integer> integerRedisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Integer> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(connectionFactory);
        redisTemplate.setEnableTransactionSupport(true);
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(new GenericToStringSerializer<>(Integer.class));
        redisTemplate.setHashKeySerializer(new StringRedisSerializer());
        redisTemplate.setHashValueSerializer(new GenericToStringSerializer<>(Integer.class));
        return redisTemplate;
    }

    
}

