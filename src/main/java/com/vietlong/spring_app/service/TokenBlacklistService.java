package com.vietlong.spring_app.service;

import com.vietlong.spring_app.config.JwtConfig;
import com.vietlong.spring_app.exception.AppException;
import com.vietlong.spring_app.exception.ErrorCode;
import com.vietlong.spring_app.model.ActiveToken;
import com.vietlong.spring_app.model.BlacklistedToken;
import com.vietlong.spring_app.repository.ActiveTokenRepository;
import com.vietlong.spring_app.repository.BlacklistedTokenRepository;
import com.vietlong.spring_app.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class TokenBlacklistService {

    private final BlacklistedTokenRepository blacklistedTokenRepository;
    private final ActiveTokenRepository activeTokenRepository;
    private final JwtConfig jwtConfig;
    private final UserRepository userRepository;

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public void blacklistToken(String jti, String userId, Date expirationTime, String reason) throws AppException {
        try {

            if (jti == null || jti.trim().isEmpty()) {
                throw new AppException(ErrorCode.BAD_REQUEST);
            }
            if (userId == null || userId.trim().isEmpty()) {
                throw new AppException(ErrorCode.BAD_REQUEST);
            }
            if (expirationTime == null) {
                throw new AppException(ErrorCode.BAD_REQUEST);
            }

            LocalDateTime expiresAt = expirationTime.toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDateTime();

            synchronized (this) {
                if (blacklistedTokenRepository.existsByJti(jti)) {
                    return;
                }

                activeTokenRepository.deleteByJti(jti);

                BlacklistedToken blacklistedToken = new BlacklistedToken(jti, userId, expiresAt, reason);
                blacklistedTokenRepository.save(blacklistedToken);
            }

        } catch (AppException e) {
            throw e;
        } catch (Exception e) {
            throw new AppException(ErrorCode.BLACKLIST_TOKEN_FAILED);
        }
    }

    @Transactional(readOnly = true)
    public boolean isTokenBlacklisted(String jti) throws AppException {
        try {
            if (jti == null || jti.trim().isEmpty()) {
                return false;
            }
            return blacklistedTokenRepository.existsByJti(jti);
        } catch (Exception e) {
            throw new AppException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    @Transactional(readOnly = true)
    public BlacklistedToken getBlacklistedToken(String jti) throws AppException {
        try {
            if (jti == null || jti.trim().isEmpty()) {
                throw new AppException(ErrorCode.BAD_REQUEST);
            }
            return blacklistedTokenRepository.findByJti(jti).orElse(null);
        } catch (AppException e) {
            throw e;
        } catch (Exception e) {
            throw new AppException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    @Transactional
    public void blacklistAllUserTokens(String userId, String reason) throws AppException {
        try {
            if (userId == null || userId.trim().isEmpty()) {
                throw new AppException(ErrorCode.BAD_REQUEST);
            }

            if (!userRepository.existsById(userId)) {
                throw new AppException(ErrorCode.USER_NOT_FOUND);
            }

            List<ActiveToken> activeTokens = activeTokenRepository.findByUserId(userId);

            for (ActiveToken activeToken : activeTokens) {

                BlacklistedToken blacklistedToken = new BlacklistedToken(
                        activeToken.getJti(),
                        activeToken.getUserId(),
                        activeToken.getExpiresAt(),
                        reason);
                blacklistedTokenRepository.save(blacklistedToken);
            }

            activeTokenRepository.deleteAllByUserId(userId);

        } catch (AppException e) {
            throw e;
        } catch (Exception e) {
            throw new AppException(ErrorCode.FORCE_LOGOUT_FAILED);
        }
    }

    @Scheduled(fixedRateString = "${spring.jwt.cleanup.expired-tokens-interval}")
    @Transactional
    public void cleanupExpiredTokens() {
        try {
            LocalDateTime now = LocalDateTime.now();
            int deletedCount = blacklistedTokenRepository.deleteExpiredTokens(now);
            if (deletedCount > 0) {
                log.info("Đã xóa {} expired blacklisted tokens", deletedCount);
            }
        } catch (Exception e) {
            log.error("Lỗi khi cleanup expired tokens: {}", e.getMessage(), e);
        }
    }

    @Scheduled(cron = "${spring.jwt.cleanup.deep-cleanup-cron}")
    @Transactional
    public void deepCleanupOldTokens() {
        try {
            LocalDateTime cutoffDate = LocalDateTime.now().minusDays(jwtConfig.getDeepCleanupDays());
            int deletedCount = blacklistedTokenRepository.deleteExpiredTokens(cutoffDate);
            if (deletedCount > 0) {
                log.info("Deep cleanup: Đã xóa {} old blacklisted tokens", deletedCount);
            }
        } catch (Exception e) {
            log.error("Lỗi khi deep cleanup old tokens: {}", e.getMessage(), e);
        }
    }

    @Scheduled(cron = "${spring.jwt.cleanup.orphaned-cleanup-cron}")
    @Transactional
    public void cleanupOrphanedTokens() {
        try {
            int deletedBlacklistedCount = blacklistedTokenRepository.deleteOrphanedBlacklistedTokens();
            int deletedActiveCount = activeTokenRepository.deleteOrphanedActiveTokens();

            if (deletedBlacklistedCount > 0 || deletedActiveCount > 0) {
                log.info("Orphaned cleanup: Đã xóa {} blacklisted tokens và {} active tokens",
                        deletedBlacklistedCount, deletedActiveCount);
            }
        } catch (Exception e) {
            log.error("Lỗi khi cleanup orphaned tokens: {}", e.getMessage(), e);
        }
    }

    @Transactional
    public int manualCleanup() throws AppException {
        try {
            LocalDateTime now = LocalDateTime.now();
            int deletedCount = blacklistedTokenRepository.deleteExpiredTokens(now);
            return deletedCount;
        } catch (Exception e) {
            throw new AppException(ErrorCode.BLACKLIST_CLEANUP_FAILED);
        }
    }

    @Transactional
    public int manualCleanupOrphaned() throws AppException {
        try {
            int deletedBlacklistedCount = blacklistedTokenRepository.deleteOrphanedBlacklistedTokens();
            int deletedActiveCount = activeTokenRepository.deleteOrphanedActiveTokens();
            return deletedBlacklistedCount + deletedActiveCount;
        } catch (Exception e) {
            throw new AppException(ErrorCode.BLACKLIST_CLEANUP_FAILED);
        }
    }

    @Transactional(readOnly = true)
    public BlacklistStats getBlacklistStats() throws AppException {
        try {
            LocalDateTime now = LocalDateTime.now();
            long totalTokens = blacklistedTokenRepository.count();

            long expiredTokens = 0;
            try {
                expiredTokens = blacklistedTokenRepository.countByExpiresAtBefore(now);
            } catch (Exception e) {
                throw new AppException(ErrorCode.BLACKLIST_STATS_FAILED);
            }

            return new BlacklistStats(totalTokens, expiredTokens, totalTokens - expiredTokens);
        } catch (Exception e) {
            throw new AppException(ErrorCode.BLACKLIST_STATS_FAILED);
        }
    }

    public static class BlacklistStats {
        private final long totalTokens;
        private final long expiredTokens;
        private final long activeTokens;

        public BlacklistStats(long totalTokens, long expiredTokens, long activeTokens) {
            this.totalTokens = totalTokens;
            this.expiredTokens = expiredTokens;
            this.activeTokens = activeTokens;
        }

        public long getTotalTokens() {
            return totalTokens;
        }

        public long getExpiredTokens() {
            return expiredTokens;
        }

        public long getActiveTokens() {
            return activeTokens;
        }

        @Override
        public String toString() {
            return String.format("Blacklist Stats - Total: %d, Expired: %d, Active: %d",
                    totalTokens, expiredTokens, activeTokens);
        }
    }

    @Transactional(readOnly = true)
    public long getBlacklistedTokenCount(String userId) throws AppException {
        try {
            if (userId == null || userId.trim().isEmpty()) {
                throw new AppException(ErrorCode.BAD_REQUEST);
            }

            if (!userRepository.existsById(userId)) {
                throw new AppException(ErrorCode.USER_NOT_FOUND);
            }

            return blacklistedTokenRepository.countByUserId(userId);
        } catch (AppException e) {
            throw e;
        } catch (Exception e) {
            throw new AppException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public void saveActiveToken(String jti, String userId, Date expirationTime, String deviceInfo) throws AppException {
        try {
            if (jti == null || jti.trim().isEmpty()) {
                throw new AppException(ErrorCode.BAD_REQUEST);
            }
            if (userId == null || userId.trim().isEmpty()) {
                throw new AppException(ErrorCode.BAD_REQUEST);
            }
            if (expirationTime == null) {
                throw new AppException(ErrorCode.BAD_REQUEST);
            }

            LocalDateTime expiresAt = expirationTime.toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDateTime();

            synchronized (this) {
                if (activeTokenRepository.existsByJti(jti)) {
                    return;
                }

                ActiveToken activeToken = new ActiveToken(jti, userId, expiresAt, deviceInfo);
                activeTokenRepository.save(activeToken);
            }

        } catch (AppException e) {
            throw e;
        } catch (Exception e) {
            throw new AppException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    @Transactional(readOnly = true)
    public List<ActiveToken> getActiveTokensByUserId(String userId) throws AppException {
        try {
            if (userId == null || userId.trim().isEmpty()) {
                throw new AppException(ErrorCode.BAD_REQUEST);
            }

            if (!userRepository.existsById(userId)) {
                throw new AppException(ErrorCode.USER_NOT_FOUND);
            }

            return activeTokenRepository.findByUserId(userId);
        } catch (AppException e) {
            throw e;
        } catch (Exception e) {
            throw new AppException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    @Scheduled(fixedRateString = "${spring.jwt.cleanup.active-tokens-interval:1800000}")
    @Transactional
    public void cleanupExpiredActiveTokens() {
        try {
            LocalDateTime now = LocalDateTime.now();
            int deletedCount = activeTokenRepository.deleteExpiredTokens(now);
            if (deletedCount > 0) {
                log.info("Đã xóa {} expired active tokens", deletedCount);
            }
        } catch (Exception e) {
            log.error("Lỗi khi cleanup expired active tokens: {}", e.getMessage(), e);
        }
    }
}
