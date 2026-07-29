package com.hiremate.dto.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {

    private Long id;
    private String email;
    private String fullName;
    private String bio;
    private String companyName;
    private boolean enabled;
    private Set<String> roles;
    private Instant createdAt;
    private Instant updatedAt;
}
