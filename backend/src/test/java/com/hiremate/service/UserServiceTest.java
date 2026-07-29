package com.hiremate.service;

import com.hiremate.common.exception.ResourceNotFoundException;
import com.hiremate.domain.entity.User;
import com.hiremate.dto.user.UserResponse;
import com.hiremate.dto.user.UserUpdateRequest;
import com.hiremate.mapper.UserMapper;
import com.hiremate.repository.UserRepository;
import com.hiremate.service.impl.UserServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    @DisplayName("Should get user by ID successfully")
    void testGetUserByIdSuccess() {
        User user = User.builder().email("john@hiremate.com").fullName("John").build();
        user.setId(1L);
        UserResponse response = UserResponse.builder().id(1L).email("john@hiremate.com").fullName("John").build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userMapper.toUserResponse(user)).thenReturn(response);

        UserResponse result = userService.getUserById(1L);

        assertNotNull(result);
        assertEquals("john@hiremate.com", result.getEmail());
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when user not found")
    void testGetUserByIdNotFound() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> userService.getUserById(99L));
    }

    @Test
    @DisplayName("Should update user profile")
    void testUpdateProfile() {
        User user = User.builder().email("john@hiremate.com").fullName("John").build();
        user.setId(1L);
        UserUpdateRequest request = UserUpdateRequest.builder().fullName("John Updated").bio("Software Eng").build();
        UserResponse response = UserResponse.builder().id(1L).fullName("John Updated").bio("Software Eng").build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(userMapper.toUserResponse(user)).thenReturn(response);

        UserResponse result = userService.updateProfile(1L, request);

        assertNotNull(result);
        assertEquals("John Updated", result.getFullName());
        verify(userRepository, times(1)).save(user);
    }
}
