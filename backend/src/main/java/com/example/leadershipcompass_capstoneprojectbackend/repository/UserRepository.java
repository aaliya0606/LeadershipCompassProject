package com.example.leadershipcompass_capstoneprojectbackend.repository;
import com.example.leadershipcompass_capstoneprojectbackend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
}
