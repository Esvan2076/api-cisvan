package com.cisvan.api.domain.searchtrending;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface SearchTrendingRepository extends JpaRepository<SearchTrending, String> {
    
    List<SearchTrending> findTop10ByOrderByWeightedScoreDesc();

    // Añadir este método para obtener más tendencias
    List<SearchTrending> findTop20ByOrderByWeightedScoreDesc();

    @Query("""
        SELECT st FROM SearchTrending st
        WHERE st.resultType IN :types
        ORDER BY st.weightedScore DESC
        LIMIT 10
    """)
    List<SearchTrending> findTop10ByTypeOrderByWeightedScoreDesc(@Param("types") List<String> types);

    @Query("""
        SELECT st FROM SearchTrending st
        WHERE st.resultType IN :types
        ORDER BY st.weightedScore DESC
        LIMIT 20
    """)
    List<SearchTrending> findTop20ByTypeOrderByWeightedScoreDesc(@Param("types") List<String> types);

    @Query("""
        SELECT st FROM SearchTrending st
        WHERE st.resultType IN :types
        ORDER BY st.weightedScore DESC
        LIMIT :limit
    """)
    List<SearchTrending> findTopByTypeOrderByWeightedScoreDesc(
        @Param("types") List<String> types,
        @Param("limit") int limit
    );
}