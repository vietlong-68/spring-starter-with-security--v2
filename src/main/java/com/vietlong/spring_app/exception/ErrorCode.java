package com.vietlong.spring_app.exception;

public enum ErrorCode {
    USER_NOT_FOUND("USER_001", "Không tìm thấy người dùng"),
    USER_ALREADY_EXISTS("USER_002", "Người dùng đã tồn tại"),
    PHONE_ALREADY_EXISTS("USER_003", "Số điện thoại đã được sử dụng"),
    USER_INVALID_INPUT("USER_004", "Dữ liệu người dùng không hợp lệ"),
    USER_UNAUTHORIZED("USER_005", "Không có quyền truy cập"),
    USER_IS_BLOCKED("USER_006", "Tài khoản đã bị khóa"),
    INTERNAL_SERVER_ERROR("SYS_001", "Lỗi hệ thống nội bộ"),
    VALIDATION_ERROR("SYS_002", "Lỗi xác thực dữ liệu"),
    RESOURCE_NOT_FOUND("SYS_003", "Không tìm thấy tài nguyên"),
    BAD_REQUEST("SYS_004", "Yêu cầu không hợp lệ"),
    METHOD_NOT_ALLOWED("SYS_005", "Phương thức HTTP không được hỗ trợ"),
    REQUEST_TIMEOUT("SYS_006", "Yêu cầu quá thời gian chờ"),
    TOO_MANY_REQUESTS("SYS_007", "Quá nhiều yêu cầu, vui lòng thử lại sau"),
    SERVICE_UNAVAILABLE("SYS_008", "Dịch vụ tạm thời không khả dụng"),
    INVALID_CREDENTIALS("SYS_009", "Tài khoản hoặc mật khẩu không hợp lệ"),
    UNKNOWN_ERROR("SYS_010", "Lỗi không xác định"),
    FILE_UPLOAD_FAILED("FILE_001", "Upload file thất bại"),
    INVALID_TOKEN("TOKEN_001", "Token không hợp lệ"),
    TOKEN_EXPIRED("TOKEN_002", "Token đã hết hạn"),
    BLACKLIST_TOKEN_FAILED("TOKEN_003", "Không thể thêm token vào blacklist"),
    BLACKLIST_CLEANUP_FAILED("TOKEN_004", "Lỗi khi dọn dẹp blacklist"),
    BLACKLIST_STATS_FAILED("TOKEN_005", "Lỗi khi lấy thống kê blacklist"),
    FORCE_LOGOUT_FAILED("TOKEN_006", "Không thể force logout user"),
    CONFIGURATION_ERROR("CONFIG_001", "Lỗi cấu hình hệ thống"),
    DATABASE_ERROR("DB_001", "Lỗi kết nối cơ sở dữ liệu"),
    NETWORK_ERROR("NET_001", "Lỗi kết nối mạng");

    private final String code;
    private final String message;

    private ErrorCode(String code, String message) {
        this.code = code;
        this.message = message;
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

}
