package com.vietlong.spring_app.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(of = { "jti", "expiresAt" })
@EqualsAndHashCode(of = { "jti" })
@Entity
@Table(name = "blacklisted_tokens", indexes = {
        @Index(name = "idx_blacklist_jti", columnList = "jti", unique = true),
        @Index(name = "idx_blacklist_expires", columnList = "expiresAt")
})
public class BlacklistedToken {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private String id;

    @Column(name = "jti", nullable = false, unique = true, length = 255)
    private String jti;

    @Column(name = "user_id", nullable = false, length = 255)
    private String userId;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @CreationTimestamp
    @Column(name = "blacklisted_at", nullable = false, updatable = false)
    private LocalDateTime blacklistedAt;

    @Column(name = "reason", length = 100)
    private String reason;

    public BlacklistedToken(String jti, String userId, LocalDateTime expiresAt, String reason) {
        this.jti = jti;
        this.userId = userId;
        this.expiresAt = expiresAt;
        this.reason = reason;
    }
}
