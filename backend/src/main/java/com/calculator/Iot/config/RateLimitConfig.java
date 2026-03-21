package com.calculator.Iot.config;

import com.calculator.Iot.service.RateLimitService;
import com.calculator.Iot.service.impl.CaffeineRateLimitService;
import com.calculator.Iot.service.impl.RedisRateLimitService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.*;
import org.springframework.context.annotation.*;
import org.springframework.data.redis.core.RedisTemplate;

/**
 * Configuração do Rate Limiting.
 *
 * Prioridade de beans:
 * 1. Se RedisTemplate estiver disponível → RedisRateLimitService
 * 2. Caso contrário → CaffeineRateLimitService (em-memory)
 *
 * O filtro (RateLimitFilter) é registrado automaticamente como @Component
 * com alta prioridade (@Order).
 */
@Configuration
@RequiredArgsConstructor
public class RateLimitConfig {

    private final RateLimitProperties rateLimitProperties;

    /**
     * RateLimitService com Redis (se disponível).
     */
    @Bean
    @ConditionalOnBean(RedisTemplate.class)
    public RateLimitService redisRateLimitService(RedisTemplate<String, String> redisTemplate) {
        return new RedisRateLimitService(redisTemplate);
    }

    /**
     * RateLimitService fallback em-memory (Caffeine).
     */
    @Bean
    @ConditionalOnMissingBean(RateLimitService.class)
    public RateLimitService caffeineRateLimitService() {
        return new CaffeineRateLimitService();
    }
}
