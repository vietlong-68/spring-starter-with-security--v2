package com.vietlong.spring_app.repository;

import com.vietlong.spring_app.model.BlacklistedToken;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface BlacklistedTokenRepository extends JpaRepository<BlacklistedToken, String> {

    boolean existsByJti(String jti);

    Optional<BlacklistedToken> findByJti(String jti);

    @Modifying
    @Query("DELETE FROM BlacklistedToken bt WHERE bt.expiresAt < :currentTime")
    int deleteExpiredTokens(@Param("currentTime") LocalDateTime currentTime);

    long countByUserId(String userId);

    @Query("SELECT COUNT(bt) FROM BlacklistedToken bt WHERE bt.expiresAt < :dateTime")
    long countByExpiresAtBefore(@Param("dateTime") LocalDateTime dateTime);

    @Modifying
    @Query("DELETE FROM BlacklistedToken bt WHERE bt.userId = :userId")
    void deleteAllByUserId(@Param("userId") String userId);

    @Modifying
    @Query("DELETE FROM BlacklistedToken bt WHERE bt.userId NOT IN (SELECT u.id FROM User u)")
    int deleteOrphanedBlacklistedTokens();
}
