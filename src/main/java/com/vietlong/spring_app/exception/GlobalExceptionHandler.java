package com.vietlong.spring_app.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.vietlong.spring_app.common.ApiResponse;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AppException.class)
    public ResponseEntity<ApiResponse<Object>> handleAppException(
            AppException ex, HttpServletRequest request) {

        ErrorCode errorCode = ex.getErrorCode();
        HttpStatus httpStatus = determineHttpStatus(errorCode);

        log.warn("Application exception occurred: {} - {} at {}",
                errorCode.getCode(), ex.getMessage(), request.getRequestURI());

        ApiResponse<Object> apiResponse = ApiResponse.error(
                ex.getMessage(),
                httpStatus.value(),
                errorCode.getCode(),
                request);

        return ResponseEntity.status(httpStatus).body(apiResponse);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Object>> handleValidationException(
            MethodArgumentNotValidException ex, HttpServletRequest request) {

        String errorMessage = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getDefaultMessage())
                .collect(Collectors.joining(", "));

        log.warn("Validation error at {}: {}", request.getRequestURI(), errorMessage);

        ApiResponse<Object> apiResponse = ApiResponse.error(
                errorMessage,
                HttpStatus.BAD_REQUEST.value(),
                ErrorCode.VALIDATION_ERROR.getCode(),
                request);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(apiResponse);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Object>> handleIllegalArgumentException(
            IllegalArgumentException ex, HttpServletRequest request) {

        log.warn("Invalid argument at {}: {}", request.getRequestURI(), ex.getMessage());

        ApiResponse<Object> apiResponse = ApiResponse.error(
                ex.getMessage(),
                HttpStatus.BAD_REQUEST.value(),
                ErrorCode.BAD_REQUEST.getCode(),
                request);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(apiResponse);
    }

    @ExceptionHandler(JwtException.class)
    public ResponseEntity<ApiResponse<Object>> handleJwtException(
            JwtException ex, HttpServletRequest request) {

        log.warn("JWT exception at {}: {}", request.getRequestURI(), ex.getMessage());

        ApiResponse<Object> apiResponse = ApiResponse.error(
                ex.getMessage(),
                HttpStatus.UNAUTHORIZED.value(),
                ErrorCode.INVALID_TOKEN.getCode(),
                request);

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(apiResponse);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Object>> handleAccessDeniedException(
            AccessDeniedException ex, HttpServletRequest request) {

        log.warn("Access denied at {}: {}", request.getRequestURI(), ex.getMessage());

        ApiResponse<Object> apiResponse = ApiResponse.error(
                "Không có quyền truy cập tài nguyên này",
                HttpStatus.FORBIDDEN.value(),
                ErrorCode.USER_UNAUTHORIZED.getCode(),
                request);

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(apiResponse);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Object>> handleGlobalException(
            Exception ex, HttpServletRequest request) {

        log.error("Unexpected error occurred at {}: {}", request.getRequestURI(), ex.getMessage(), ex);

        ApiResponse<Object> apiResponse = ApiResponse.error(
                ErrorCode.INTERNAL_SERVER_ERROR.getMessage(),
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                ErrorCode.INTERNAL_SERVER_ERROR.getCode(),
                request);

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(apiResponse);
    }

    private HttpStatus determineHttpStatus(ErrorCode errorCode) {
        if (errorCode == null) {
            return HttpStatus.INTERNAL_SERVER_ERROR;
        }
        switch (errorCode) {
            case USER_NOT_FOUND:
                return HttpStatus.NOT_FOUND;
            case USER_ALREADY_EXISTS:
                return HttpStatus.CONFLICT;
            case USER_INVALID_INPUT:
                return HttpStatus.BAD_REQUEST;
            case USER_UNAUTHORIZED:
                return HttpStatus.UNAUTHORIZED;
            case USER_IS_BLOCKED:
                return HttpStatus.FORBIDDEN;
            case PHONE_ALREADY_EXISTS:
                return HttpStatus.CONFLICT;

            case INTERNAL_SERVER_ERROR:
                return HttpStatus.INTERNAL_SERVER_ERROR;
            case VALIDATION_ERROR:
                return HttpStatus.BAD_REQUEST;
            case RESOURCE_NOT_FOUND:
                return HttpStatus.NOT_FOUND;
            case BAD_REQUEST:
                return HttpStatus.BAD_REQUEST;
            case METHOD_NOT_ALLOWED:
                return HttpStatus.METHOD_NOT_ALLOWED;
            case REQUEST_TIMEOUT:
                return HttpStatus.REQUEST_TIMEOUT;
            case TOO_MANY_REQUESTS:
                return HttpStatus.TOO_MANY_REQUESTS;
            case SERVICE_UNAVAILABLE:
                return HttpStatus.SERVICE_UNAVAILABLE;
            case INVALID_CREDENTIALS:
                return HttpStatus.UNAUTHORIZED;
            case UNKNOWN_ERROR:
                return HttpStatus.INTERNAL_SERVER_ERROR;

            case FILE_UPLOAD_FAILED:
                return HttpStatus.UNPROCESSABLE_ENTITY;

            case INVALID_TOKEN:
            case TOKEN_EXPIRED:
                return HttpStatus.UNAUTHORIZED;
            case BLACKLIST_TOKEN_FAILED:
            case BLACKLIST_CLEANUP_FAILED:
            case BLACKLIST_STATS_FAILED:
            case FORCE_LOGOUT_FAILED:
                return HttpStatus.INTERNAL_SERVER_ERROR;

            case CONFIGURATION_ERROR:
                return HttpStatus.INTERNAL_SERVER_ERROR;
            case DATABASE_ERROR:
                return HttpStatus.SERVICE_UNAVAILABLE;
            case NETWORK_ERROR:
                return HttpStatus.SERVICE_UNAVAILABLE;

            default:
                return HttpStatus.INTERNAL_SERVER_ERROR;
        }
    }
}
