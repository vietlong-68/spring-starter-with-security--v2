package com.vietlong.spring_app.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class IntrospectTokenRequest {
    @NotBlank(message = "Token không được để trống")
    private String token;

}
