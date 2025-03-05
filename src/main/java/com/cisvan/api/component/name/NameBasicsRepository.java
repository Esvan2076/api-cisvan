package com.cisvan.api.component.name;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NameBasicsRepository extends JpaRepository<NameBasics, String> {
    List<NameBasics> findByPrimaryNameContainingIgnoreCase(String primaryName);
}
