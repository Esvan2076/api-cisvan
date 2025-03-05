package com.cisvan.api.component.ratings;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TitleRatingsRepository extends JpaRepository<TitleRatings, String> {

}
