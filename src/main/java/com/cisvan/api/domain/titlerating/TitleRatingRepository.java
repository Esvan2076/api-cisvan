package com.cisvan.api.domain.titlerating;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface TitleRatingRepository extends JpaRepository<TitleRating, String> {

    @Query("SELECT tr.averageRating FROM TitleRating tr WHERE tr.tconst = :tconst")
    Optional<BigDecimal> findRatingByTconst(@Param("tconst") String tconst);
    
    @Query(value = "SELECT tr.tconst FROM TitleRating tr " +
           "JOIN Title t ON tr.tconst = t.tconst " +
           "WHERE tr.tconst NOT IN (:excludedTitles) AND " +
           "t.titleType != 'tvEpisode' " +
           "ORDER BY tr.averageRating DESC, tr.numVotes DESC " +
           "LIMIT :limit")
    List<String> findTopRatedTitles(@Param("excludedTitles") List<String> excludedTitles, 
                                @Param("limit") int limit);
}
