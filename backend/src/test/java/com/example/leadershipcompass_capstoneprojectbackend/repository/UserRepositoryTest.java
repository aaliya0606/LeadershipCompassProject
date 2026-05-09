package com.example.leadershipcompass_capstoneprojectbackend.repository;

// Imports the Role enum from the model package
import com.example.leadershipcompass_capstoneprojectbackend.model.Role;

// Imports the User entity from the model package
import com.example.leadershipcompass_capstoneprojectbackend.model.User;

// JUnit test annotation
import org.junit.jupiter.api.Test;

import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

// Allows Spring to inject the repository automatically
import org.springframework.beans.factory.annotation.Autowired;


// Activates the application-test.properties configuration
import org.springframework.test.context.ActiveProfiles;

// Imports assertion methods used for checking expected results
import static org.junit.jupiter.api.Assertions.*;

// Loads only JPA-related components for lightweight database testing
@DataJpaTest

// Uses the "test" profile so H2 database settings are applied
@ActiveProfiles("test")

class UserRepositoryTest {

    // Injects the real UserRepository into the test class
    @Autowired
    private UserRepository userRepository;

    // Marks this method as a test case
    @Test
    void shouldSaveUser() {

        // Creates a new User object using the builder pattern
        User user = User.builder()
                .fullName("Tori")
                .email("tori@test.com")
                .password("password123")
                .role(Role.USER)
                .build();

        // Saves the user into the H2 in-memory database
        User savedUser = userRepository.save(user);

        // Checks that the user ID was automatically generated
        assertNotNull(savedUser.getId());

        // Checks that the email was stored correctly
        assertEquals("tori@test.com", savedUser.getEmail());
    }

    @Test
    void shouldFindUserByEmail() {

        User user = User.builder()
                .fullName("Tori")
                .email("tori@test.com")
                .password("password123")
                .role(Role.USER)
                .build();

        userRepository.save(user);

        assertTrue(userRepository.findByEmail("tori@test.com").isPresent());
    }

    @Test
    void shouldReturnTrueWhenEmailExists() {

        User user = User.builder()
                .fullName("Tori")
                .email("tori@test.com")
                .password("password123")
                .role(Role.USER)
                .build();

        userRepository.save(user);

        assertTrue(userRepository.existsByEmail("tori@test.com"));
    }
}

