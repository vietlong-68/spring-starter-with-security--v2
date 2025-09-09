package com.vietlong.spring_app.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;
import lombok.Getter;

@Getter
@Configuration
public class JwtConfig {

    @Value("${spring.jwt.signer-key}")
    private String signerKey;

    @Value("${spring.jwt.expiration-hours}")
    private int expirationHours;

    @Value("${spring.jwt.cleanup.expired-tokens-interval}")
    private long expiredTokensCleanupInterval;

    @Value("${spring.jwt.cleanup.deep-cleanup-cron}")
    private String deepCleanupCron;

    @Value("${spring.jwt.cleanup.orphaned-cleanup-cron}")
    private String orphanedCleanupCron;

    @Value("${spring.jwt.cleanup.deep-cleanup-days}")
    private int deepCleanupDays;

    @Value("${spring.jwt.cleanup.active-tokens-interval}")
    private long activeTokensCleanupInterval;

    @PostConstruct
    public void validateConfiguration() {
        if (signerKey == null || signerKey.trim().isEmpty()) {
            throw new IllegalStateException("JWT signer key cannot be null or empty");
        }
        if (signerKey.length() < 32) {
            throw new IllegalStateException("JWT signer key must be at least 32 characters long");
        }
        if (expirationHours <= 0) {
            throw new IllegalStateException("JWT expiration hours must be positive");
        }
        if (expiredTokensCleanupInterval <= 0) {
            throw new IllegalStateException("Expired tokens cleanup interval must be positive");
        }
        if (activeTokensCleanupInterval <= 0) {
            throw new IllegalStateException("Active tokens cleanup interval must be positive");
        }
        if (deepCleanupDays <= 0) {
            throw new IllegalStateException("Deep cleanup days must be positive");
        }
        if (deepCleanupCron == null || deepCleanupCron.trim().isEmpty()) {
            throw new IllegalStateException("Deep cleanup cron expression cannot be null or empty");
        }
        if (orphanedCleanupCron == null || orphanedCleanupCron.trim().isEmpty()) {
            throw new IllegalStateException("Orphaned cleanup cron expression cannot be null or empty");
        }
    }

}
