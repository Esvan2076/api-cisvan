package com.cisvan.api.domain.principal.repos;

import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.cisvan.api.domain.name.dto.NameEssencialDTO;
import com.cisvan.api.domain.principal.Principal;
import com.cisvan.api.domain.principal.PrincipalId;
import com.cisvan.api.domain.reviews.dtos.TitleOrderingDTO;

@Repository
public interface PrincipalRepository extends JpaRepository<Principal, PrincipalId> {

    @Query("SELECT ta FROM Principal ta WHERE ta.id.tconst = :tconst")
    List<Principal> findByTitleId(@Param("tconst") String tconst);

    List<Principal> findByIdTconst(String tconst);

    @Query("""
                SELECT tp.nconst FROM Principal tp
                WHERE tp.id.tconst = :tconst AND tp.category IN ('actor', 'actress')
                ORDER BY tp.id.ordering ASC
            """)
    List<String> findFirst2ActorsByTconst(@Param("tconst") String tconst, PageRequest pageable);

    @Query("""
                SELECT DISTINCT new com.cisvan.api.domain.name.dto.NameEssencialDTO(n.nconst, n.primaryName)
                FROM Principal p
                JOIN Name n ON p.nconst = n.nconst
                WHERE p.id.tconst = :tconst AND p.category IN :categories
                GROUP BY n.nconst, n.primaryName
            """)
    List<NameEssencialDTO> findActorsAndActressesByTconst(@Param("tconst") String tconst,
            @Param("categories") List<String> categories);

    @Query(value = """
                SELECT n.nconst, n.primary_name
                FROM title_crew tc
                JOIN unnest(tc.directors) AS director_id ON true
                JOIN name_basics n ON director_id = n.nconst
                WHERE tc.tconst = :tconst
                LIMIT 3
            """, nativeQuery = true)
    List<Object[]> findDirectorsByTconst(@Param("tconst") String tconst);

    @Query("SELECT p FROM Principal p WHERE p.id.tconst = :tconst AND (p.category = 'actor' OR p.category = 'actress')")
    List<Principal> findActorsByTconst(@Param("tconst") String tconst);

    @Query("SELECT p.nconst FROM Principal p WHERE p.id.tconst = :tconst " +
            "AND p.category = 'actor' OR p.category = 'actress' ORDER BY p.id.ordering ASC")
    List<String> findActorsByTconstRecomendation(@Param("tconst") String tconst);

    @Query("SELECT new com.cisvan.api.domain.reviews.dtos.TitleOrderingDTO(p.id.tconst, p.id.ordering) " +
            "FROM Principal p JOIN Title t ON p.id.tconst = t.tconst " +
            "WHERE p.nconst = :nconst " +
            "AND (p.category = 'actor' OR p.category = 'actress') " +
            "AND t.titleType != 'tvEpisode'")
    List<TitleOrderingDTO> findTitlesAndOrderingByNconst(@Param("nconst") String nconst);

    boolean existsById_TconstAndNconst(String tconst, String nconst);

    @Query("""
                SELECT p FROM Principal p
                WHERE p.id.tconst = :tconst
                AND p.category IN :categories
                ORDER BY p.id.ordering ASC
            """)
    List<Principal> findByTitleTconstAndCategoryIn(
            @Param("tconst") String tconst,
            @Param("categories") List<String> categories);
}
