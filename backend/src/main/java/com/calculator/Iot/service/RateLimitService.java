package com.calculator.Iot.service;

import java.time.Duration;

/**
 * Interface para serviço de Rate Limiting.
 * Implementa algoritmo Sliding Window Log usando Sorted Sets no Redis.
 * Para fallback em-memory, usa Caffeine cache.
 */
public interface RateLimitService {

    /**
     * Verifica se uma requisição está permitida baseada no identificador.
     *
     * @param identifier identificador único (IP, user ID, API key)
     * @param maxRequests número máximo de requisições permitidas
     * @param windowDuration período de tempo (Duration)
     * @return true se permitido, false se limite excedido
     */
    boolean isAllowed(String identifier, int maxRequests, Duration windowDuration);

    /**
     * Retorna o número de requisições restantes para um identificador.
     *
     * @param identifier identificador único
     * @param maxRequests limite máximo
     * @param windowDuration período de tempo
     * @return requisições restantes (mínimo 0)
     */
    int getRemainingRequests(String identifier, int maxRequests, Duration windowDuration);

    /**
     * Retorna o tempo em segundos até o reset do contador.
     *
     * @param identifier identificador único
     * @param windowDuration período de tempo
     * @return segundos até reset (0 se já resetou)
     */
    long getTimeToReset(String identifier, Duration windowDuration);

    /**
     * Limpa as entradas expiradas do cache (método de manutenção).
     */
    void cleanup();
}
