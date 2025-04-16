package com.cisvan.api.domain.namerating;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NameRatingRepository extends JpaRepository<NameRating, String> {

}
