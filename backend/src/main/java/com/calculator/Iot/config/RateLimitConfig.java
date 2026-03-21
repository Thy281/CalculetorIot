package com.calculator.Iot.config;

import com.calculator.Iot.filter.RateLimitFilter;
import com.calculator.Iot.service.RateLimitService;
import com.calculator.Iot.service.impl.CaffeineRateLimitService;
import com.calculator.Iot.service.impl.RedisRateLimitService;
import jakarta.servlet.Filter;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.*;
import org.springframework.boot.web.servlet.server.FilterRegistrationBean;
import org.springframework.context.annotation.*;
import org.springframework.core.Ordered;
import org.springframework.data.redis.core.RedisTemplate;

/**
 * Configuração do Rate Limiting.
 *
 * Prioridade de beans:
 * 1. Se RedisTemplate estiver disponível → RedisRateLimitService
 * 2. Caso contrário → CaffeineRateLimitService (em-memory)
 *
 * O filtro é registrado com ordem mais alta (executa antes de outros filtros).
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

    /**
     * Registra o filtro de rate limiting.
     * Ordem: executa antes da maioria dos outros filtros.
     */
    @Bean
    @ConditionalOnProperty(name = "ratelimit.enabled", havingValue = "true", matchIfMissing = true)
    public FilterRegistrationBean<RateLimitFilter> rateLimitFilter(RateLimitService rateLimitService) {
        FilterRegistrationBean<RateLimitFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new RateLimitFilter(rateLimitService, rateLimitProperties));
        registration.addUrlPatterns("/*");
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE + 10); // executar cedo
        registration.setName("rateLimitFilter");
        return registration;
    }
}
