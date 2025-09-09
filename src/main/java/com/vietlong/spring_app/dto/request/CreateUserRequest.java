package com.vietlong.spring_app.dto.request;

import com.vietlong.spring_app.model.Gender;
import com.vietlong.spring_app.validation.PastOrPresent;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;

@Data
public class CreateUserRequest {

    @NotBlank(message = "Tên hiển thị không được để trống")
    @Size(min = 2, max = 100, message = "Tên hiển thị phải có từ 2 đến 100 ký tự")
    private String displayName;

    @NotBlank(message = "Email không được để trống")
    @Email(message = "Email không hợp lệ")
    @Size(max = 255, message = "Email không được vượt quá 255 ký tự")
    private String email;

    @NotBlank(message = "Mật khẩu không được để trống")
    @Size(min = 8, max = 128, message = "Mật khẩu phải có từ 8 đến 128 ký tự")
    private String password;

    @Pattern(regexp = "^[0-9]{10,11}$", message = "Số điện thoại phải có 10-11 chữ số")
    private String phoneNumber;

    @PastOrPresent(message = "Ngày sinh không được ở trong tương lai")
    private LocalDate dateOfBirth;

    private Gender gender;

    private Boolean isEmailVerified = false;

    private Boolean isPhoneVerified = false;

    @Size(max = 500, message = "Địa chỉ không được vượt quá 500 ký tự")
    private String address;
}
