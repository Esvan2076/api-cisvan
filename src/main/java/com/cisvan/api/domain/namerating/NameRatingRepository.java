package com.cisvan.api.domain.namerating;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NameRatingRepository extends JpaRepository<NameRating, String> {

    List<NameRating> findTop100ByOrderByNumVotesDesc();
}
