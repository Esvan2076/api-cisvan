package com.cisvan.api.domain.title.repos;

import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.cisvan.api.domain.title.Title;
import com.cisvan.api.domain.title.dtos.TitleShowDTO;

@Repository
public interface TitleRepository extends JpaRepository<Title, String>, TitleCustomRepository {
    
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

    @Query("""
        SELECT new com.cisvan.api.domain.title.dtos.TitleShowDTO(
            b.tconst, 
            b.primaryTitle, 
            b.posterUrl, 
            r.averageRating
        )
        FROM Title b
        JOIN TitleRating r ON b.tconst = r.tconst
        WHERE b.titleType IN ('tvSeries', 'tvMiniSeries')
        ORDER BY r.averageRating DESC, r.numVotes DESC
        LIMIT 20
    """)
    List<TitleShowDTO> findTop20Series();
    
    @Query("""
        SELECT new com.cisvan.api.domain.title.dtos.TitleShowDTO(
            b.tconst, 
            b.primaryTitle, 
            b.posterUrl, 
            r.averageRating
        )
        FROM Title b
        JOIN TitleRating r ON b.tconst = r.tconst
        WHERE b.titleType IN ('movie')
        ORDER BY r.averageRating DESC, r.numVotes DESC
        LIMIT 20
    """)
    List<TitleShowDTO> findTop20NonSeries();
    
    @Query("""
        SELECT new com.cisvan.api.domain.title.dtos.TitleShowDTO(
            b.tconst,
            b.primaryTitle,
            b.posterUrl,
            r.averageRating
        )
        FROM Title b
        JOIN TitleRating r ON b.tconst = r.tconst
        JOIN Trending tc ON tc.contentId = b.tconst
        WHERE b.titleType != 'tvEpisode'
        ORDER BY tc.score DESC
        LIMIT 20
    """)
    List<TitleShowDTO> findTrendingContent();
    
    @Query("""
        SELECT new com.cisvan.api.domain.title.dtos.TitleShowDTO(
            b.tconst,
            b.primaryTitle,
            b.posterUrl,
            r.averageRating
        )
        FROM Title b
        JOIN TitleRating r ON b.tconst = r.tconst
        WHERE b.tconst IN :contentIds
        ORDER BY r.numVotes DESC, r.averageRating DESC
        LIMIT 20
    """)
    List<TitleShowDTO> findTrendingContentDetails(@Param("contentIds") List<String> contentIds);    

    @Query("""
        SELECT t.contentId
        FROM Trending t
        JOIN Title tb ON t.contentId = tb.tconst
        WHERE tb.titleType NOT IN ('tvEpisode')
        ORDER BY t.score DESC
        LIMIT 20
    """)
    List<String> findTop20TrendingContentIds();

    @Query("""
        SELECT new com.cisvan.api.domain.title.dtos.TitleShowDTO(
            b.tconst,
            b.primaryTitle,
            b.posterUrl,
            r.averageRating
        )
        FROM Title b
        JOIN TitleRating r ON b.tconst = r.tconst
        WHERE b.tconst IN :tconstList
    """)
    List<TitleShowDTO> findByTconstIn(@Param("tconstList") List<String> tconstList);

    @Query(value = """
        SELECT
            CASE WHEN ep.parent_tconst IS NOT NULL THEN ep.parent_tconst ELSE b.tconst END AS resolved_tconst,
            b.primary_title,
            COALESCE(parent.poster_url, b.poster_url) AS resolved_poster_url,
            r.average_rating,
            ul.title_id
        FROM user_list ul
        JOIN title_basics b ON ul.title_id = b.tconst
        LEFT JOIN title_episode ep ON ul.title_id = ep.tconst
        LEFT JOIN title_basics parent ON ep.parent_tconst = parent.tconst
        LEFT JOIN title_ratings r ON b.tconst = r.tconst
        WHERE ul.user_id = :userId
        ORDER BY ul.created_at DESC
        """, nativeQuery = true)
    List<Object[]> findTitlesInUserListWithResolvedPosters(@Param("userId") Long userId);

    // Exact genre match (exclude tvEpisode)
    @Query(value = """
        SELECT 
            tb.tconst,
            tb.title_type,
            tb.primary_title,
            tb.start_year,
            tb.end_year,
            tr.average_rating,
            tr.num_votes,
            tb.poster_url
        FROM title_basics tb
        JOIN title_ratings tr ON tb.tconst = tr.tconst
        WHERE tb.genres @> CAST(:genres AS VARCHAR[])
          AND tb.title_type != 'tvEpisode'
          AND tb.tconst != :excludedTconst
        ORDER BY tr.average_rating DESC
        LIMIT 10
    """, nativeQuery = true)
    List<Object[]> findTop10ByGenresContainingAll(
        @Param("genres") String genresArrayLiteral,
        @Param("excludedTconst") String tconst
    );

    @Query(value = """
        SELECT 
            tb.tconst,
            tb.title_type,
            tb.primary_title,
            tb.start_year,
            tb.end_year,
            tr.average_rating,
            tr.num_votes,
            tb.poster_url
        FROM title_basics tb
        JOIN title_ratings tr ON tb.tconst = tr.tconst
        WHERE tb.genres && CAST(:genres AS VARCHAR[]) 
        AND tb.title_type != 'tvEpisode'
        AND tb.tconst NOT IN (:excludedTconsts)
        ORDER BY tr.average_rating DESC
        LIMIT :limit
    """, nativeQuery = true)
    List<Object[]> findTopByAnyMatchingGenreExcluding(
        @Param("genres") String genresArrayLiteral,
        @Param("excludedTconsts") List<String> excludedTconsts,
        @Param("limit") int limit
    );
}
