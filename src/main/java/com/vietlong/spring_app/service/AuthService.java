package com.vietlong.spring_app.service;

import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import jakarta.servlet.http.HttpServletRequest;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSObject;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.Payload;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.vietlong.spring_app.common.JwtConstants;
import com.vietlong.spring_app.common.Mapper;
import com.vietlong.spring_app.common.TimeConstants;
import com.vietlong.spring_app.config.JwtConfig;
import com.vietlong.spring_app.dto.request.IntrospectTokenRequest;
import com.vietlong.spring_app.dto.request.LoginRequest;
import com.vietlong.spring_app.dto.request.RegisterRequest;
import com.vietlong.spring_app.dto.response.IntrospectTokenResponse;
import com.vietlong.spring_app.dto.response.LoginResponse;
import com.vietlong.spring_app.dto.response.UserResponse;
import com.vietlong.spring_app.exception.AppException;
import com.vietlong.spring_app.exception.ErrorCode;
import com.vietlong.spring_app.model.Role;
import com.vietlong.spring_app.model.User;
import com.vietlong.spring_app.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtConfig jwtConfig;
    private final TokenBlacklistService tokenBlacklistService;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtConfig jwtConfig,
            TokenBlacklistService tokenBlacklistService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtConfig = jwtConfig;
        this.tokenBlacklistService = tokenBlacklistService;
    }

    public UserResponse handleRegister(RegisterRequest registerRequest) throws AppException {

        if (!registerRequest.getPassword().equals(registerRequest.getConfirmPassword())) {
            throw new AppException(ErrorCode.VALIDATION_ERROR, "Mật khẩu và xác nhận mật khẩu không khớp");
        }

        Optional<User> existingUser = userRepository.findByEmail(registerRequest.getEmail());
        if (existingUser.isPresent()) {
            throw new AppException(ErrorCode.USER_ALREADY_EXISTS);
        }

        if (registerRequest.getPhoneNumber() != null && !registerRequest.getPhoneNumber().trim().isEmpty()) {
            Optional<User> existingUserByPhone = userRepository.findByPhoneNumber(registerRequest.getPhoneNumber());
            if (existingUserByPhone.isPresent()) {
                throw new AppException(ErrorCode.PHONE_ALREADY_EXISTS);
            }
        }

        User user = new User();
        user.setDisplayName(registerRequest.getDisplayName());
        user.setEmail(registerRequest.getEmail());
        user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        user.setRole(Role.USER);
        user.setPhoneNumber(registerRequest.getPhoneNumber());
        user.setDateOfBirth(registerRequest.getDateOfBirth());
        user.setGender(registerRequest.getGender());
        user.setIsEmailVerified(false);
        user.setIsPhoneVerified(false);
        user.setAddress(registerRequest.getAddress());

        User savedUser = userRepository.save(user);
        return Mapper.convertToUserResponse(savedUser);
    }

    public LoginResponse handleLogin(LoginRequest loginRequest, HttpServletRequest request) throws AppException {
        String email = loginRequest.getEmail();

        Optional<User> userOptional = userRepository.findByEmail(email);
        if (!userOptional.isPresent()) {
            throw new AppException(ErrorCode.USER_NOT_FOUND);
        }

        User user = userOptional.get();

        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            throw new AppException(ErrorCode.INVALID_CREDENTIALS);
        }

        String token = generateToken(user);

        try {
            JWTClaimsSet claimsSet = verifyToken(token);
            String jti = claimsSet.getJWTID();
            Date expirationTime = claimsSet.getExpirationTime();
            String deviceInfo = extractDeviceInfo(request);

            tokenBlacklistService.saveActiveToken(jti, user.getId(), expirationTime, deviceInfo);
        } catch (Exception e) {
            log.error("Lỗi khi lưu active token cho user {}: {}", user.getEmail(), e.getMessage(), e);
        }

        return LoginResponse.builder()
                .success(true)
                .token(token)
                .build();
    }

    public IntrospectTokenResponse introspectToken(IntrospectTokenRequest request) {
        String token = request.getToken();

        try {
            JWTClaimsSet claimsSet = verifyToken(token);

            Date expirationTime = claimsSet.getExpirationTime();
            if (expirationTime != null && expirationTime.before(new Date())) {
                return IntrospectTokenResponse.builder()
                        .isValid(false)
                        .build();
            }

            if (!claimsSet.getAudience().contains(JwtConstants.AUDIENCE)) {
                return IntrospectTokenResponse.builder()
                        .isValid(false)
                        .build();
            }

            String scope = claimsSet.getStringClaim(JwtConstants.SCOPE_CLAIM);
            String email = claimsSet.getStringClaim(JwtConstants.EMAIL_CLAIM);
            String jti = claimsSet.getJWTID();
            Date issuedAt = claimsSet.getIssueTime();

            LocalDateTime expiresAt = null;
            LocalDateTime issuedAtLocal = null;

            if (expirationTime != null) {
                expiresAt = expirationTime.toInstant()
                        .atZone(java.time.ZoneId.systemDefault())
                        .toLocalDateTime();
            }

            if (issuedAt != null) {
                issuedAtLocal = issuedAt.toInstant()
                        .atZone(java.time.ZoneId.systemDefault())
                        .toLocalDateTime();
            }

            return IntrospectTokenResponse.builder()
                    .isValid(true)
                    .scope(scope)
                    .clientId(JwtConstants.AUDIENCE)
                    .username(email)
                    .expiresAt(expiresAt)
                    .issuedAt(issuedAtLocal)
                    .jti(jti)
                    .build();

        } catch (Exception e) {
            log.warn("Token introspection failed: {}", e.getMessage());
            return IntrospectTokenResponse.builder()
                    .isValid(false)
                    .build();
        }
    }

    private JWTClaimsSet verifyToken(String token) throws Exception {
        try {
            SignedJWT signedJWT = SignedJWT.parse(token);
            JWSVerifier verifier = new MACVerifier(jwtConfig.getSignerKey().getBytes(StandardCharsets.UTF_8));
            boolean isValid = signedJWT.verify(verifier);

            if (!isValid) {
                throw new Exception("Token signature không hợp lệ");
            }

            return signedJWT.getJWTClaimsSet();
        } catch (JOSEException | ParseException e) {
            throw new Exception("Token không hợp lệ: " + e.getMessage());
        }
    }

    private String generateToken(User user) throws AppException {
        JWSHeader jwsHeader = new JWSHeader(JWSAlgorithm.HS512);

        JWTClaimsSet jwtClaimsSet = new JWTClaimsSet.Builder()
                .subject(user.getEmail())
                .issuer(JwtConstants.ISSUER)
                .audience(JwtConstants.AUDIENCE)
                .issueTime(new Date())
                .expirationTime(new Date(System.currentTimeMillis()
                        + TimeConstants.MILLISECONDS_PER_HOUR * jwtConfig.getExpirationHours()))
                .jwtID(UUID.randomUUID().toString())
                .claim(JwtConstants.USER_ID_CLAIM, user.getId())
                .claim(JwtConstants.EMAIL_CLAIM, user.getEmail())
                .claim(JwtConstants.SCOPE_CLAIM, user.getRole().name())
                .build();

        JWSObject jwsObject = new JWSObject(jwsHeader, new Payload(jwtClaimsSet.toJSONObject()));

        try {
            jwsObject.sign(new MACSigner(jwtConfig.getSignerKey().getBytes(StandardCharsets.UTF_8)));
        } catch (JOSEException e) {
            throw new AppException(ErrorCode.INTERNAL_SERVER_ERROR);
        }

        return jwsObject.serialize();
    }

    private org.springframework.security.oauth2.jwt.Jwt getJwtFromAuthentication(Authentication authentication)
            throws AppException {
        if (authentication.getPrincipal() instanceof org.springframework.security.oauth2.jwt.Jwt) {
            return (org.springframework.security.oauth2.jwt.Jwt) authentication.getPrincipal();
        }
        throw new AppException(ErrorCode.INVALID_TOKEN);
    }

    public String getJtiFromAuthentication(Authentication authentication) throws AppException {
        return getJwtFromAuthentication(authentication).getId();
    }

    public String getUserIdFromAuthentication(Authentication authentication) throws AppException {
        return getJwtFromAuthentication(authentication).getClaimAsString("userId");
    }

    public Date getExpirationTimeFromAuthentication(Authentication authentication) throws AppException {
        return Date.from(getJwtFromAuthentication(authentication).getExpiresAt());
    }

    public Map<String, Object> getCurrentUserProfile(Authentication authentication, HttpServletRequest request)
            throws AppException {

        if (authentication == null) {
            throw new AppException(ErrorCode.BAD_REQUEST, "Authentication cannot be null");
        }
        if (request == null) {
            throw new AppException(ErrorCode.BAD_REQUEST, "HttpServletRequest cannot be null");
        }

        Map<String, Object> userProfile = new HashMap<>();

        try {

            Map<String, Object> authInfo = buildAuthenticationInfo(authentication);
            userProfile.put("authentication", authInfo);

            if (authentication.getPrincipal() instanceof org.springframework.security.oauth2.jwt.Jwt) {
                Map<String, Object> jwtInfo = buildJwtInfo(
                        (org.springframework.security.oauth2.jwt.Jwt) authentication.getPrincipal());
                userProfile.put("jwt", jwtInfo);
            }

            Map<String, Object> securityContextInfo = buildSecurityContextInfo();
            userProfile.put("securityContext", securityContextInfo);

            Map<String, Object> requestInfo = buildRequestInfo(request);
            userProfile.put("request", requestInfo);

            Map<String, Object> timeInfo = buildTimeInfo();
            userProfile.put("timestamp", timeInfo);

        } catch (Exception e) {
            Map<String, Object> errorInfo = new HashMap<>();
            errorInfo.put("error", e.getMessage());
            errorInfo.put("errorClass", e.getClass().getSimpleName());
            userProfile.put("error", errorInfo);
        }

        return userProfile;
    }

    private Map<String, Object> buildAuthenticationInfo(Authentication authentication) {
        Map<String, Object> authInfo = new HashMap<>();
        authInfo.put("authenticated", authentication.isAuthenticated());
        authInfo.put("name", authentication.getName());
        authInfo.put("principal", authentication.getPrincipal().getClass().getSimpleName());

        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        authInfo.put("authorities", authorities.stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList()));

        return authInfo;
    }

    private Map<String, Object> buildJwtInfo(org.springframework.security.oauth2.jwt.Jwt jwt) {
        Map<String, Object> jwtInfo = new HashMap<>();
        jwtInfo.put("jti", jwt.getId());
        jwtInfo.put("subject", jwt.getSubject());
        jwtInfo.put("issuer", jwt.getIssuer());
        jwtInfo.put("audience", jwt.getAudience());
        jwtInfo.put("issuedAt", jwt.getIssuedAt());
        jwtInfo.put("expiresAt", jwt.getExpiresAt());
        jwtInfo.put("notBefore", jwt.getNotBefore());

        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", jwt.getClaimAsString("userId"));
        claims.put("email", jwt.getClaimAsString("email"));
        claims.put("scope", jwt.getClaimAsString("scope"));
        claims.put("role", jwt.getClaimAsString("scope"));

        jwtInfo.put("customClaims", claims);
        return jwtInfo;
    }

    private Map<String, Object> buildSecurityContextInfo() {
        Map<String, Object> securityContextInfo = new HashMap<>();
        securityContextInfo.put("contextClass", SecurityContextHolder.getContext().getClass().getSimpleName());
        securityContextInfo.put("authenticationClass",
                SecurityContextHolder.getContext().getAuthentication().getClass().getSimpleName());
        return securityContextInfo;
    }

    private Map<String, Object> buildRequestInfo(HttpServletRequest request) {
        Map<String, Object> requestInfo = new HashMap<>();
        requestInfo.put("requestURI", request.getRequestURI());
        requestInfo.put("method", request.getMethod());
        requestInfo.put("remoteAddr", request.getRemoteAddr());
        requestInfo.put("userAgent", request.getHeader("User-Agent"));
        requestInfo.put("sessionId",
                request.getSession(false) != null ? request.getSession().getId() : "No session");
        return requestInfo;
    }

    private Map<String, Object> buildTimeInfo() {
        Map<String, Object> timeInfo = new HashMap<>();
        timeInfo.put("currentTime", LocalDateTime.now());
        timeInfo.put("timezone", ZoneId.systemDefault().toString());
        return timeInfo;
    }

    private String extractDeviceInfo(HttpServletRequest request) {
        if (request == null) {
            return "Unknown";
        }

        String userAgent = request.getHeader("User-Agent");
        if (userAgent == null || userAgent.isEmpty()) {
            return "Unknown";
        }

        String lowerUserAgent = userAgent.toLowerCase();

        if (isMobileDevice(lowerUserAgent)) {
            return "Mobile Browser";
        }

        if (isDesktopBrowser(lowerUserAgent)) {
            return "Desktop Browser";
        }

        if (isTabletDevice(lowerUserAgent)) {
            return "Tablet Browser";
        }

        if (isApiClient(lowerUserAgent)) {
            return "API Client";
        }

        if (isBotOrCrawler(lowerUserAgent)) {
            return "Bot/Crawler";
        }

        return "Unknown";
    }

    private boolean isMobileDevice(String userAgent) {
        return userAgent.contains("mobile") ||
                userAgent.contains("android") ||
                userAgent.contains("iphone") ||
                userAgent.contains("windows phone");
    }

    private boolean isDesktopBrowser(String userAgent) {
        return userAgent.contains("windows") ||
                userAgent.contains("macintosh") ||
                userAgent.contains("linux") ||
                userAgent.contains("x11");
    }

    private boolean isTabletDevice(String userAgent) {
        return userAgent.contains("tablet") ||
                userAgent.contains("ipad");
    }

    private boolean isApiClient(String userAgent) {
        return userAgent.contains("postman") ||
                userAgent.contains("curl") ||
                userAgent.contains("wget") ||
                userAgent.contains("java") ||
                userAgent.contains("okhttp") ||
                userAgent.contains("apache-httpclient");
    }

    private boolean isBotOrCrawler(String userAgent) {
        return userAgent.contains("bot") ||
                userAgent.contains("crawler") ||
                userAgent.contains("spider") ||
                userAgent.contains("scraper");
    }

}
