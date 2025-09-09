package com.vietlong.spring_app.controller.admin;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.vietlong.spring_app.common.ApiResponse;
import com.vietlong.spring_app.dto.request.CreateUserRequest;
import com.vietlong.spring_app.dto.request.UpdateUserRequest;
import com.vietlong.spring_app.dto.response.UserResponse;
import com.vietlong.spring_app.exception.AppException;
import com.vietlong.spring_app.service.admin.AdminUserService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/admin/users")
public class AdminUserController {
    private final AdminUserService adminUserService;

    public AdminUserController(AdminUserService adminUserService) {
        this.adminUserService = adminUserService;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<UserResponse>>> getAllUsers(HttpServletRequest httpRequest) {
        List<UserResponse> users = adminUserService.getAllUsers();
        ApiResponse<List<UserResponse>> response = ApiResponse.success(users, "lấy danh sách người dùng thành công",
                httpRequest);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/paginated")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Page<UserResponse>>> getAllUsersPaginated(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            HttpServletRequest httpRequest) {
        Page<UserResponse> users = adminUserService.getAllUsersPaginated(page, size, sortBy, sortDir);
        ApiResponse<Page<UserResponse>> response = ApiResponse.success(users, "lấy danh sách người dùng thành công",
                httpRequest);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<UserResponse>> getUserById(
            @PathVariable(name = "userId") String userId,
            HttpServletRequest httpRequest) throws AppException {
        UserResponse user = adminUserService.getUserById(userId);
        ApiResponse<UserResponse> response = ApiResponse.success(user, "lấy thông tin người dùng thành công",
                httpRequest);
        return ResponseEntity.ok(response);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<UserResponse>> createUser(
            @Valid @RequestBody CreateUserRequest request,
            HttpServletRequest httpRequest) throws AppException {
        UserResponse user = adminUserService.createUser(request);
        ApiResponse<UserResponse> response = ApiResponse.success(user, "tạo người dùng thành công", httpRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PatchMapping("/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<UserResponse>> updateUser(
            @PathVariable(name = "userId") String userId,
            @Valid @RequestBody UpdateUserRequest request,
            HttpServletRequest httpRequest) throws AppException {
        UserResponse user = adminUserService.updateUser(userId, request);
        ApiResponse<UserResponse> response = ApiResponse.success(user, "cập nhật người dùng thành công", httpRequest);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteUser(
            @PathVariable(name = "userId") String userId,
            HttpServletRequest httpRequest) throws AppException {
        adminUserService.deleteUser(userId);
        ApiResponse<Void> response = ApiResponse.success(null, "xóa người dùng thành công", httpRequest);
        return ResponseEntity.ok(response);
    }
}
