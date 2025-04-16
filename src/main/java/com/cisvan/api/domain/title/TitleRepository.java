package com.cisvan.api.domain.title;

import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface TitleRepository extends JpaRepository<Title, String>{
    
    List<Title> findByPrimaryTitleContainingIgnoreCase(String primaryName);

    List<Title> findTop5ByPrimaryTitleIgnoreCaseContainingOrderByPrimaryTitleAsc(String titlePart);

    @Query("""
        SELECT t FROM Title t
        JOIN TitleRating r ON t.tconst = r.tconst
        WHERE t.titleType = 'movie'
        AND LOWER(t.primaryTitle) LIKE LOWER(CONCAT(:query, '%'))
        ORDER BY r.numVotes DESC
    """)
    List<Title> findTop5ByTitleWithRatingOrder(@Param("query") String query, PageRequest pageable);

    @Query("""
        SELECT t FROM Title t
        JOIN TitleRating r ON t.tconst = r.tconst
        WHERE t.titleType = 'tvSeries'
        AND LOWER(t.primaryTitle) LIKE LOWER(CONCAT(:query, '%'))
        ORDER BY r.numVotes DESC
    """)
    List<Title> findTop5SeriesWithRatingOrder(@Param("query") String query, PageRequest pageable);
}
