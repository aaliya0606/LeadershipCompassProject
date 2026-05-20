package com.example.leadershipcompass_capstoneprojectbackend.repository;

import com.example.leadershipcompass_capstoneprojectbackend.model.Modules;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ModulesRepository extends JpaRepository<Modules, Long> {
    Optional<Modules> findByBookAndTitle(String book, String title);
}
