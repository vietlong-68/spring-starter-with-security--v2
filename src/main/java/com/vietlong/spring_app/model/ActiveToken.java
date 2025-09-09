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
@ToString(of = { "jti", "userId", "expiresAt" })
@EqualsAndHashCode(of = { "jti" })
@Entity
@Table(name = "active_tokens", indexes = {
        @Index(name = "idx_active_token_jti", columnList = "jti", unique = true),
        @Index(name = "idx_active_token_user", columnList = "userId"),
        @Index(name = "idx_active_token_expires", columnList = "expiresAt")
})
public class ActiveToken {

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
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "device_info", length = 500)
    private String deviceInfo;

    public ActiveToken(String jti, String userId, LocalDateTime expiresAt, String deviceInfo) {
        this.jti = jti;
        this.userId = userId;
        this.expiresAt = expiresAt;
        this.deviceInfo = deviceInfo;
    }
}
