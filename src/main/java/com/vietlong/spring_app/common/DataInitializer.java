package com.vietlong.spring_app.common;

import com.vietlong.spring_app.model.Role;
import com.vietlong.spring_app.model.User;
import com.vietlong.spring_app.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${ADMIN_EMAIL:vietlong@vietlong.com}")
    private String adminEmail;

    @Value("${ADMIN_PASSWORD:12345678}")
    private String adminPassword;

    @Value("${ADMIN_DISPLAY_NAME:VietLong}")
    private String adminDisplayName;

    @Value("${ADMIN_PHONE:0123456789}")
    private String adminPhone;

    @Value("${ADMIN_ADDRESS:Hà Nội, Việt Nam}")
    private String adminAddress;

    @Override
    public void run(String... args) throws Exception {
        initializeDefaultAdmin();
    }

    private void initializeDefaultAdmin() {
        Optional<User> existingAdmin = userRepository.findByEmail(adminEmail);

        if (existingAdmin.isPresent()) {
            log.info("Admin user đã tồn tại với email: {}", adminEmail);
            return;
        }

        if (adminPassword.length() < 8) {
            log.warn("Admin password quá ngắn (tối thiểu 8 ký tự). Sử dụng password mặc định.");
            adminPassword = "12345678";
        }

        User adminUser = User.builder()
                .displayName(adminDisplayName)
                .email(adminEmail)
                .password(passwordEncoder.encode(adminPassword))
                .role(Role.ADMIN)
                .phoneNumber(adminPhone)
                .isEmailVerified(true)
                .isPhoneVerified(true)
                .address(adminAddress)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        try {
            userRepository.save(adminUser);
            log.info("Đã tạo admin user mặc định thành công!");
            log.info("Email: {}", adminEmail);
            log.info("Display Name: {}", adminDisplayName);
            log.info("Phone: {}", adminPhone);
            log.info("Address: {}", adminAddress);
            log.warn("Mật khẩu admin: {} (Hãy đổi mật khẩu sau khi đăng nhập lần đầu)", adminPassword);
        } catch (Exception e) {
            log.error("Lỗi khi tạo admin user mặc định: {}", e.getMessage());

        }
    }
}
