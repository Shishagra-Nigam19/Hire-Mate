package com.hiremate.repository;

import com.hiremate.domain.entity.Role;
import com.hiremate.domain.entity.User;
import com.hiremate.domain.enums.RoleType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class UserRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("Should find user by email with fetch join on roles")
    void testFindByEmail() {
        Role role = Role.builder().name(RoleType.ROLE_CANDIDATE).description("Candidate role").build();
        role = entityManager.persistAndFlush(role);

        User user = User.builder()
                .email("testcandidate@hiremate.com")
                .password("encoded_pass")
                .fullName("Test Candidate")
                .enabled(true)
                .emailVerified(true)
                .roles(Set.of(role))
                .build();

        entityManager.persistAndFlush(user);

        Optional<User> foundUser = userRepository.findByEmail("testcandidate@hiremate.com");

        assertTrue(foundUser.isPresent());
        assertEquals("Test Candidate", foundUser.get().getFullName());
        assertEquals(1, foundUser.get().getRoles().size());
    }

    @Test
    @DisplayName("Should return true when email exists")
    void testExistsByEmail() {
        User user = User.builder()
                .email("exists@hiremate.com")
                .password("encoded_pass")
                .fullName("Existing User")
                .enabled(true)
                .build();

        entityManager.persistAndFlush(user);

        assertTrue(userRepository.existsByEmail("exists@hiremate.com"));
        assertFalse(userRepository.existsByEmail("nonexistent@hiremate.com"));
    }
}
