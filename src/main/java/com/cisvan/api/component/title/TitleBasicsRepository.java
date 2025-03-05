package com.cisvan.api.component.title;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TitleBasicsRepository extends JpaRepository<TitleBasics, String>{
    List<TitleBasics> findByPrimaryTitleContainingIgnoreCase(String primaryName);
}
