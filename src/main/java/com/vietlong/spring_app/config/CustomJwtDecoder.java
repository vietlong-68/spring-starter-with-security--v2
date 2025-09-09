package com.vietlong.spring_app.config;

import javax.crypto.spec.SecretKeySpec;

import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.stereotype.Component;

import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.vietlong.spring_app.common.JwtConstants;
import com.vietlong.spring_app.service.TokenBlacklistService;

import jakarta.annotation.PostConstruct;

@Component
public class CustomJwtDecoder implements JwtDecoder {

    private final JwtConfig jwtConfig;
    private final TokenBlacklistService tokenBlacklistService;
    private NimbusJwtDecoder nimbusJwtDecoder = null;

    public CustomJwtDecoder(JwtConfig jwtConfig, TokenBlacklistService tokenBlacklistService) {
        this.jwtConfig = jwtConfig;
        this.tokenBlacklistService = tokenBlacklistService;
    }

    @PostConstruct
    private void initializeDecoder() {
        SecretKeySpec secretKeySpec = new SecretKeySpec(jwtConfig.getSignerKey().getBytes(), JwtConstants.ALGORITHM);
        nimbusJwtDecoder = NimbusJwtDecoder.withSecretKey(secretKeySpec)
                .macAlgorithm(MacAlgorithm.HS512)
                .build();
    }

    @Override
    public Jwt decode(String token) throws JwtException {
        try {
            SignedJWT signedJWT = SignedJWT.parse(token);
            JWTClaimsSet claimsSet = signedJWT.getJWTClaimsSet();

            String jti = claimsSet.getJWTID();
            if (jti != null) {
                try {
                    if (tokenBlacklistService.isTokenBlacklisted(jti)) {
                        throw new JwtException("Token đã bị logout");
                    }
                } catch (Exception e) {
                    throw new JwtException("Lỗi kiểm tra blacklist: " + e.getMessage());
                }
            }

        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new JwtException("Token không hợp lệ");
        }

        return nimbusJwtDecoder.decode(token);
    }
}