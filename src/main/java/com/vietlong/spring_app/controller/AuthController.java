package com.vietlong.spring_app.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.vietlong.spring_app.common.ApiResponse;
import com.vietlong.spring_app.dto.request.IntrospectTokenRequest;
import com.vietlong.spring_app.dto.request.LoginRequest;
import com.vietlong.spring_app.dto.request.RegisterRequest;
import com.vietlong.spring_app.dto.response.IntrospectTokenResponse;
import com.vietlong.spring_app.dto.response.LoginResponse;
import com.vietlong.spring_app.dto.response.UserResponse;
import com.vietlong.spring_app.exception.AppException;
import com.vietlong.spring_app.exception.ErrorCode;
import com.vietlong.spring_app.service.AuthService;
import com.vietlong.spring_app.service.TokenBlacklistService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.Date;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final TokenBlacklistService tokenBlacklistService;

    public AuthController(AuthService authService, TokenBlacklistService tokenBlacklistService) {
        this.authService = authService;
        this.tokenBlacklistService = tokenBlacklistService;
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<UserResponse>> register(@Valid @RequestBody RegisterRequest registerRequest,
            HttpServletRequest request) throws AppException {
        UserResponse userResponse = authService.handleRegister(registerRequest);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(userResponse, "Đăng ký tài khoản thành công", request));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@RequestBody LoginRequest loginRequest,
            HttpServletRequest request) throws AppException {
        LoginResponse loginResponse = authService.handleLogin(loginRequest, request);
        return ResponseEntity.ok(ApiResponse.success(loginResponse, "Đăng nhập thành công", request));
    }

    @PostMapping("/introspect")
    public ResponseEntity<ApiResponse<IntrospectTokenResponse>> introspectToken(
            @Valid @RequestBody IntrospectTokenRequest introspectRequest,
            HttpServletRequest request) {
        IntrospectTokenResponse introspectResponse = authService.introspectToken(introspectRequest);

        String message = introspectResponse.getIsValid() ? "Token hợp lệ" : "Token không hợp lệ";

        return ResponseEntity.ok(ApiResponse.success(introspectResponse, message, request));
    }

    @PostMapping("/logout")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> logout(
            Authentication authentication,
            HttpServletRequest request) throws AppException {

        try {
            String jti = authService.getJtiFromAuthentication(authentication);
            String userId = authService.getUserIdFromAuthentication(authentication);
            Date expirationTime = authService.getExpirationTimeFromAuthentication(authentication);

            tokenBlacklistService.blacklistToken(jti, userId, expirationTime, "LOGOUT");

            return ResponseEntity.ok(ApiResponse.success(null, "Đăng xuất thành công", request));

        } catch (AppException e) {
            throw e;
        } catch (Exception e) {
            throw new AppException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

}