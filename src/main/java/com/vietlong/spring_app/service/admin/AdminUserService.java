package com.vietlong.spring_app.service.admin;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.vietlong.spring_app.common.Mapper;
import com.vietlong.spring_app.dto.request.CreateUserRequest;
import com.vietlong.spring_app.dto.request.UpdateUserRequest;
import com.vietlong.spring_app.dto.response.UserResponse;
import com.vietlong.spring_app.exception.AppException;
import com.vietlong.spring_app.exception.ErrorCode;
import com.vietlong.spring_app.model.Role;
import com.vietlong.spring_app.model.User;
import com.vietlong.spring_app.repository.UserRepository;

@Service
public class AdminUserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminUserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(Mapper::convertToUserResponse)
                .collect(java.util.stream.Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Page<UserResponse> getAllUsersPaginated(int page, int size, String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<User> userPage = userRepository.findAll(pageable);

        return userPage.map(Mapper::convertToUserResponse);
    }

    public UserResponse getUserById(String userId) throws AppException {
        User user = findUserById(userId);
        return Mapper.convertToUserResponse(user);
    }

    @Transactional
    public UserResponse createUser(CreateUserRequest createUserRequest) throws AppException {
        Optional<User> existingUser = userRepository.findByEmail(createUserRequest.getEmail());
        if (existingUser.isPresent()) {
            throw new AppException(ErrorCode.USER_ALREADY_EXISTS);
        }

        if (createUserRequest.getPhoneNumber() != null) {
            Optional<User> existingUserByPhone = userRepository.findByPhoneNumber(createUserRequest.getPhoneNumber());
            if (existingUserByPhone.isPresent()) {
                throw new AppException(ErrorCode.PHONE_ALREADY_EXISTS);
            }
        }

        User user = new User();
        user.setDisplayName(createUserRequest.getDisplayName());
        user.setEmail(createUserRequest.getEmail());
        user.setPassword(passwordEncoder.encode(createUserRequest.getPassword()));
        user.setRole(Role.USER);
        user.setPhoneNumber(createUserRequest.getPhoneNumber());
        user.setDateOfBirth(createUserRequest.getDateOfBirth());
        user.setGender(createUserRequest.getGender());
        user.setIsEmailVerified(createUserRequest.getIsEmailVerified());
        user.setIsPhoneVerified(createUserRequest.getIsPhoneVerified());
        user.setAddress(createUserRequest.getAddress());

        User savedUser = userRepository.save(user);
        return Mapper.convertToUserResponse(savedUser);
    }

    @Transactional
    public UserResponse updateUser(String userId, UpdateUserRequest updateUserRequest) throws AppException {
        User user = findUserById(userId);

        if (updateUserRequest.getDisplayName() != null && !updateUserRequest.getDisplayName().trim().isEmpty()) {
            user.setDisplayName(updateUserRequest.getDisplayName());
        }
        if (updateUserRequest.getPassword() != null && !updateUserRequest.getPassword().trim().isEmpty()) {
            user.setPassword(passwordEncoder.encode(updateUserRequest.getPassword()));
        }
        if (updateUserRequest.getPhoneNumber() != null && !updateUserRequest.getPhoneNumber().trim().isEmpty()) {
            Optional<User> existingUserByPhone = userRepository.findByPhoneNumber(updateUserRequest.getPhoneNumber());
            if (existingUserByPhone.isPresent()) {
                User existingUserEntity = existingUserByPhone.get();
                if (!existingUserEntity.getId().equals(userId)) {
                    throw new AppException(ErrorCode.PHONE_ALREADY_EXISTS);
                }
            }
            user.setPhoneNumber(updateUserRequest.getPhoneNumber());
        }

        if (updateUserRequest.getDateOfBirth() != null) {
            user.setDateOfBirth(updateUserRequest.getDateOfBirth());
        }
        if (updateUserRequest.getGender() != null) {
            user.setGender(updateUserRequest.getGender());
        }
        if (updateUserRequest.getAddress() != null && !updateUserRequest.getAddress().trim().isEmpty()) {
            user.setAddress(updateUserRequest.getAddress());
        }

        User updatedUser = userRepository.save(user);
        return Mapper.convertToUserResponse(updatedUser);
    }

    @Transactional
    public void deleteUser(String userId) throws AppException {
        User user = findUserById(userId);
        userRepository.delete(user);
    }

    private User findUserById(String userId) throws AppException {
        return userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
    }

}
