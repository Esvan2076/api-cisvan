package com.cisvan.api.domain.titlerating;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TitleRatingRepository extends JpaRepository<TitleRating, String> {

}
