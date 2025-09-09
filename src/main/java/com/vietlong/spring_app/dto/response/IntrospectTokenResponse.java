package com.vietlong.spring_app.dto.response;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class IntrospectTokenResponse {

    private Boolean isValid;
    private String scope;
    private String clientId;
    private String username;
    private LocalDateTime expiresAt;
    private LocalDateTime issuedAt;
    private String jti;
}
