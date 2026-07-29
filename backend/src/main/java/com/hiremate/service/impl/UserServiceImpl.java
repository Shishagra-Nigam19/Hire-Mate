package com.hiremate.service.impl;

import com.hiremate.common.exception.ResourceNotFoundException;
import com.hiremate.common.sanitization.SanitizerUtil;
import com.hiremate.domain.entity.User;
import com.hiremate.dto.user.UserResponse;
import com.hiremate.dto.user.UserUpdateRequest;
import com.hiremate.mapper.UserMapper;
import com.hiremate.repository.UserRepository;
import com.hiremate.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Override
    @Transactional(readOnly = true)
    public UserResponse getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
        return userMapper.toUserResponse(user);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getCurrentUserProfile(Long currentUserId) {
        return getUserById(currentUserId);
    }

    @Override
    @Transactional
    public UserResponse updateProfile(Long currentUserId, UserUpdateRequest request) {
        log.info("Updating profile for user ID: {}", currentUserId);
        User user = userRepository.findById(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", currentUserId));

        if (request.getFullName() != null) {
            user.setFullName(SanitizerUtil.sanitizeStrict(request.getFullName()));
        }
        if (request.getBio() != null) {
            user.setBio(SanitizerUtil.sanitize(request.getBio()));
        }
        if (request.getCompanyName() != null) {
            user.setCompanyName(SanitizerUtil.sanitizeStrict(request.getCompanyName()));
        }

        User updatedUser = userRepository.save(user);
        return userMapper.toUserResponse(updatedUser);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserResponse> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable)
                .map(userMapper::toUserResponse);
    }
}
