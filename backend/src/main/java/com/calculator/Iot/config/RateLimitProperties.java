package com.calculator.Iot.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Propriedades de configuração para Rate Limiting.
 * Configurável via application.yml/application.properties
 */
@Data
@Component
@ConfigurationProperties(prefix = "ratelimit")
public class RateLimitProperties {

    /**
     * Se o rate limiting está habilitado
     */
    private boolean enabled = true;

    /**
     * Número máximo de requisições permitidas no período
     */
    private int maxRequests = 100;

    /**
     * Período em minutos
     */
    private int periodMinutes = 15;

    /**
     * Tipo de identificador: "ip", "user" (para usuários autenticados), "api-key"
     */
    private String identifierType = "ip";

    /**
     * Lista de endpoints que estarão isentos de rate limiting (padrão: actuator endpoints)
     */
    private String[] excludedPaths = {"/actuator/**", "/error"};

    /**
     * Configurações específicas por endpoint (opcional)
     * Exemplo:
     * endpoints:
     *   - path: /api/calculate
     *     maxRequests: 200
     *     periodMinutes: 5
     */
    private Map<String, EndpointConfig> endpoints;

    @Data
    public static class EndpointConfig {
        private int maxRequests;
        private int periodMinutes;
    }
}
