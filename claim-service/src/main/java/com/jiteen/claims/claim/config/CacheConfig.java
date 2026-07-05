package com.jiteen.claims.claim.config;

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
 * Redis distributed caching infrastructure configuration for the Claim Service.
 *
 * <p>
 * Activates Spring's declarative caching abstraction via {@link EnableCaching} and
 * registers a {@link RedisCacheManager} backed by a Lettuce connection pool. Two
 * named caches are configured with independent TTLs to match their respective access
 * patterns:
 * </p>
 * <ul>
 *   <li>{@value CacheNames#CLAIMS} — individual claim lookups by UUID; 15-minute TTL
 *       balances freshness against DB load for frequently accessed claim records.</li>
 *   <li>{@value CacheNames#CLAIMS_LIST} — full active-claim list; 5-minute TTL
 *       minimises staleness risk because list results become outdated whenever any
 *       claim is created, updated, approved, rejected, or deleted.</li>
 * </ul>
 *
 * <p>
 * Values are serialized to JSON using {@link GenericJackson2JsonRedisSerializer}
 * with a custom {@link ObjectMapper} that embeds type metadata ({@code @class})
 * to support correct polymorphic deserialization without type headers. Keys are
 * plain UTF-8 strings via {@link StringRedisSerializer}.
 * </p>
 *
 * <p>
 * To disable caching without code changes (e.g., in tests), set
 * {@code spring.cache.type: none} in the active profile's application properties.
 * Spring Boot's auto-configuration will then replace this {@link RedisCacheManager}
 * with a no-op implementation.
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
     * TTL in seconds for entries in the {@value CacheNames#CLAIMS} cache.
     * Default: 900 seconds (15 minutes).
     */
    @Value("${cache.ttl.claims:900}")
    private long claimsTtlSeconds;

    /**
     * TTL in seconds for entries in the {@value CacheNames#CLAIMS_LIST} cache.
     * Default: 300 seconds (5 minutes).
     */
    @Value("${cache.ttl.claims-list:300}")
    private long claimsListTtlSeconds;

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
        // JDK serialization is used for the claim caches because the values
        // (ClaimResponse and List<ClaimResponse>) cannot be round-tripped by
        // GenericJackson2JsonRedisSerializer with polymorphic default typing —
        // an empty list serializes in a form Jackson then fails to deserialize.
        cacheConfigurations.put(CacheNames.CLAIMS,
                buildJdkCacheConfig(Duration.ofSeconds(claimsTtlSeconds)));
        cacheConfigurations.put(CacheNames.CLAIMS_LIST,
                buildJdkCacheConfig(Duration.ofSeconds(claimsListTtlSeconds)));

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
        // Embed @class type metadata so GenericJackson2JsonRedisSerializer can
        // reconstruct the correct type on deserialization without type headers.
        mapper.activateDefaultTyping(
                BasicPolymorphicTypeValidator.builder()
                        .allowIfBaseType(Object.class)
                        .build(),
                ObjectMapper.DefaultTyping.NON_FINAL,
                JsonTypeInfo.As.PROPERTY);
        return mapper;
    }
}
