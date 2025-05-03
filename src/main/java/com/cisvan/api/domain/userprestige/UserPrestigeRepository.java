package com.cisvan.api.domain.userprestige;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserPrestigeRepository extends JpaRepository<UserPrestige, Long> {
    // Long is userId (PK)
}