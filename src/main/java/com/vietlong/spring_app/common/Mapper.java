package com.vietlong.spring_app.common;

import com.vietlong.spring_app.dto.response.UserResponse;
import com.vietlong.spring_app.model.User;

public class Mapper {
    public static UserResponse convertToUserResponse(User user) {
        UserResponse userResponse = new UserResponse();
        userResponse.setId(user.getId());
        userResponse.setDisplayName(user.getDisplayName());
        userResponse.setEmail(user.getEmail());
        userResponse.setRole(user.getRole());
        userResponse.setPhoneNumber(user.getPhoneNumber());
        userResponse.setAvatarUrl(user.getAvatarUrl());
        userResponse.setDateOfBirth(user.getDateOfBirth());
        userResponse.setGender(user.getGender());
        userResponse.setIsEmailVerified(user.getIsEmailVerified());
        userResponse.setIsPhoneVerified(user.getIsPhoneVerified());
        userResponse.setAddress(user.getAddress());
        userResponse.setCreatedAt(user.getCreatedAt());
        userResponse.setUpdatedAt(user.getUpdatedAt());
        return userResponse;
    }
}
