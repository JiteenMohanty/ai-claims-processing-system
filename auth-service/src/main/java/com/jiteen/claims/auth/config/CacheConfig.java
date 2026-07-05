package com.jiteen.claims.auth.config;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.JdkSerializationRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * Redis distributed caching infrastructure configuration for the Auth Service.
 *
 * <p>
 * Activates Spring's declarative caching abstraction via {@link EnableCaching} and
 * registers a {@link RedisCacheManager} with a dedicated per-cache TTL map.
 * </p>
 *
 * <p>
 * The primary cache configured here is {@value CacheNames#USERS}, which stores
 * {@link org.springframework.security.core.userdetails.UserDetails} objects keyed by
 * normalized email address. This cache is populated by
 * {@code CustomUserDetailsService.loadUserByUsername()}, which is invoked by the JWT
 * authentication filter on every authenticated request. Without caching, each request
 * triggers a database lookup; with caching, the lookup is short-circuited for the TTL
 * duration, dramatically reducing DB round-trips under load.
 * </p>
 *
 * <p>
 * Entries are evicted from the {@value CacheNames#USERS} cache on user logout to
 * prevent stale {@code UserDetails} from being used after session revocation.
 * </p>
 *
 * @author Jiteen
 * @version 1.0
 * @since Java 21
 */
@Configuration
@EnableCaching
public class CacheConfig {

    /**
     * TTL in seconds for entries in the {@value CacheNames#USERS} cache.
     * Default: 1800 seconds (30 minutes).
     */
    @Value("${cache.ttl.users:1800}")
    private long usersTtlSeconds;

    /**
     * Constructs the primary {@link RedisCacheManager} with per-cache TTL configuration.
     *
     * @param connectionFactory the auto-configured Lettuce Redis connection factory
     * @return a fully configured {@link RedisCacheManager}
     */
    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        RedisCacheConfiguration defaultConfig = buildDefaultCacheConfig(Duration.ofMinutes(10));

        Map<String, RedisCacheConfiguration> cacheConfigurations = new HashMap<>();
        cacheConfigurations.put(CacheNames.USERS,
                buildJdkCacheConfig(Duration.ofSeconds(usersTtlSeconds)));

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(defaultConfig)
                .withInitialCacheConfigurations(cacheConfigurations)
                .build();
    }

    private RedisCacheConfiguration buildJdkCacheConfig(Duration ttl) {
        return RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(ttl)
                .disableCachingNullValues()
                .serializeKeysWith(
                        RedisSerializationContext.SerializationPair
                                .fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(
                        RedisSerializationContext.SerializationPair
                                .fromSerializer(new JdkSerializationRedisSerializer()));
    }

    private RedisCacheConfiguration buildDefaultCacheConfig(Duration ttl) {
        return RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(ttl)
                .disableCachingNullValues()
                .serializeKeysWith(
                        RedisSerializationContext.SerializationPair
                                .fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(
                        RedisSerializationContext.SerializationPair
                                .fromSerializer(new GenericJackson2JsonRedisSerializer(buildObjectMapper())));
    }

    private ObjectMapper buildObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        mapper.activateDefaultTyping(
                BasicPolymorphicTypeValidator.builder()
                        .allowIfBaseType(Object.class)
                        .build(),
                ObjectMapper.DefaultTyping.NON_FINAL,
                JsonTypeInfo.As.PROPERTY);
        return mapper;
    }
}
