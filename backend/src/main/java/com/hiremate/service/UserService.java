package com.hiremate.service;

import com.hiremate.dto.user.UserResponse;
import com.hiremate.dto.user.UserUpdateRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserService {

    UserResponse getUserById(Long id);

    UserResponse getCurrentUserProfile(Long currentUserId);

    UserResponse updateProfile(Long currentUserId, UserUpdateRequest request);

    Page<UserResponse> getAllUsers(Pageable pageable);
}
