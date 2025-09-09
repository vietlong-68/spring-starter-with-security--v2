package com.vietlong.spring_app.dto.response;

import com.vietlong.spring_app.model.Gender;
import com.vietlong.spring_app.model.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {
    private String id;
    private String displayName;
    private String email;
    private Role role;
    private String phoneNumber;
    private String avatarUrl;
    private LocalDate dateOfBirth;
    private Gender gender;
    private Boolean isEmailVerified;
    private Boolean isPhoneVerified;
    private String address;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
