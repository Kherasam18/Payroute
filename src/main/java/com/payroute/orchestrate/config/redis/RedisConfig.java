package com.payroute.orchestrate.config.redis;

import com.payroute.orchestrate.domain.dto.PaymentResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * Redis template configuration.
 *
 * <p>Provides two templates:
 * <ul>
 *   <li>{@code paymentResponseRedisTemplate} — stores structured PaymentResponse (idempotency cache)</li>
 *   <li>{@code lockRedisTemplate} — stores plain strings (distributed locks)</li>
 * </ul>
 *
 * <p>Conditionally loaded only when a {@link RedisConnectionFactory} bean is available,
 * allowing tests to exclude Redis auto-configuration without errors.</p>
 */
@Slf4j
@Configuration
@ConditionalOnBean(RedisConnectionFactory.class)
public class RedisConfig {

    @Bean("paymentResponseRedisTemplate")
    public RedisTemplate<String, PaymentResponse> paymentResponseRedisTemplate(
            RedisConnectionFactory connectionFactory) {

        RedisTemplate<String, PaymentResponse> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new Jackson2JsonRedisSerializer<>(PaymentResponse.class));
        template.afterPropertiesSet();

        log.info("Configured paymentResponseRedisTemplate with Jackson2Json serializer");
        return template;
    }

    @Bean("lockRedisTemplate")
    public RedisTemplate<String, String> lockRedisTemplate(
            RedisConnectionFactory connectionFactory) {

        RedisTemplate<String, String> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new StringRedisSerializer());
        template.afterPropertiesSet();

        log.info("Configured stringRedisTemplate for distributed locks");
        return template;
    }
}
