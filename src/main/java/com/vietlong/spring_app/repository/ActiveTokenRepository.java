package com.vietlong.spring_app.repository;

import com.vietlong.spring_app.model.ActiveToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ActiveTokenRepository extends JpaRepository<ActiveToken, String> {

    boolean existsByJti(String jti);

    Optional<ActiveToken> findByJti(String jti);

    List<ActiveToken> findByUserId(String userId);

    @Modifying
    @Query("DELETE FROM ActiveToken at WHERE at.expiresAt < :currentTime")
    int deleteExpiredTokens(@Param("currentTime") LocalDateTime currentTime);

    @Modifying
    @Query("DELETE FROM ActiveToken at WHERE at.userId = :userId")
    void deleteAllByUserId(@Param("userId") String userId);

    @Modifying
    @Query("DELETE FROM ActiveToken at WHERE at.jti = :jti")
    void deleteByJti(@Param("jti") String jti);

    long countByUserId(String userId);

    @Query("SELECT COUNT(at) FROM ActiveToken at WHERE at.expiresAt < :dateTime")
    long countByExpiresAtBefore(@Param("dateTime") LocalDateTime dateTime);

    @Modifying
    @Query("DELETE FROM ActiveToken at WHERE at.userId NOT IN (SELECT u.id FROM User u)")
    int deleteOrphanedActiveTokens();
}
