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

/**
 * Repository test class for testing UserRepository database operations.
 *
 * <p>This class uses Spring Boot's @DataJpaTest annotation to load only
 * JPA-related components and test repository functionality against the
 * H2 in-memory database configured in the test profile.</p>
 *
 * <p>The tests validate that users can be saved, searched, and checked
 * for existence correctly within the database.</p>
 */

// Loads only JPA-related components for lightweight database testing
@DataJpaTest

// Uses the "test" profile so H2 database settings are applied
@ActiveProfiles("test")

class UserRepositoryTest {

    // Injects the real UserRepository into the test class
    @Autowired
    private UserRepository userRepository;

    /**
     * Tests whether a User entity can be successfully saved into the database.
     *
     * <p>This test verifies that:
     * <ul>
     *     <li>The user is stored successfully</li>
     *     <li>An ID is automatically generated</li>
     *     <li>The saved email matches the expected value</li>
     * </ul>
     * </p>
     */

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

    /**
     * Tests whether a user can be retrieved using their email address.
     *
     * <p>This test verifies that the findByEmail() repository method
     * correctly returns a user when a matching email exists
     * in the database.</p>
     */

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

    /**
     * Tests whether the repository correctly identifies
     * an existing email address.
     *
     * <p>This test verifies that the existsByEmail() method
     * returns true when a user with the provided email
     * already exists in the database.</p>
     */

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

