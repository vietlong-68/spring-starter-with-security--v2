package com.vietlong.spring_app.controller.admin;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.vietlong.spring_app.common.ApiResponse;
import com.vietlong.spring_app.exception.AppException;
import com.vietlong.spring_app.model.ActiveToken;
import com.vietlong.spring_app.service.TokenBlacklistService;
import com.vietlong.spring_app.service.TokenBlacklistService.BlacklistStats;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("/api/admin/blacklist")
public class AdminBlacklistController {

    private final TokenBlacklistService tokenBlacklistService;

    public AdminBlacklistController(TokenBlacklistService tokenBlacklistService) {
        this.tokenBlacklistService = tokenBlacklistService;
    }

    @GetMapping("/stats")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<BlacklistStats>> getBlacklistStats(HttpServletRequest request)
            throws AppException {
        BlacklistStats stats = tokenBlacklistService.getBlacklistStats();
        return ResponseEntity.ok(ApiResponse.success(stats, "Lấy thống kê blacklist thành công", request));
    }

    @PostMapping("/cleanup")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Integer>> manualCleanup(HttpServletRequest request) throws AppException {
        int deletedCount = tokenBlacklistService.manualCleanup();
        String message = deletedCount > 0 ? "Đã xóa " + deletedCount + " token blacklist đã hết hạn"
                : "Không có token nào cần xóa";
        return ResponseEntity.ok(ApiResponse.success(deletedCount, message, request));
    }

    @GetMapping("/user/{userId}/count")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Long>> getUserBlacklistCount(
            @PathVariable(name = "userId") String userId,
            HttpServletRequest request) throws AppException {
        long count = tokenBlacklistService.getBlacklistedTokenCount(userId);
        return ResponseEntity.ok(ApiResponse.success(count, "Lấy số lượng token blacklist thành công", request));
    }

    @PostMapping("/user/{userId}/force-logout")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<String>> forceLogoutUser(
            @PathVariable(name = "userId") String userId,
            @RequestParam(name = "reason", defaultValue = "ADMIN_FORCE_LOGOUT") String reason,
            HttpServletRequest request) throws AppException {
        tokenBlacklistService.blacklistAllUserTokens(userId, reason);
        return ResponseEntity.ok(ApiResponse.success("SUCCESS", "Force logout user thành công", request));
    }

    @GetMapping("/user/{userId}/active-tokens")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<ActiveToken>>> getUserActiveTokens(
            @PathVariable(name = "userId") String userId,
            HttpServletRequest request) throws AppException {
        List<ActiveToken> activeTokens = tokenBlacklistService.getActiveTokensByUserId(userId);
        return ResponseEntity.ok(ApiResponse.success(activeTokens, "Lấy danh sách active token thành công", request));
    }

    @PostMapping("/cleanup-orphaned")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Integer>> cleanupOrphanedTokens(HttpServletRequest request) throws AppException {
        int deletedCount = tokenBlacklistService.manualCleanupOrphaned();
        String message = deletedCount > 0 ? "Đã xóa " + deletedCount + " orphaned token"
                : "Không có orphaned token nào cần xóa";
        return ResponseEntity.ok(ApiResponse.success(deletedCount, message, request));
    }
}
