package com.cisvan.api.domain.title;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TitleRepository extends JpaRepository<Title, String>{
    
    List<Title> findByPrimaryTitleContainingIgnoreCase(String primaryName);
}
