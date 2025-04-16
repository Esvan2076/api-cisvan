package com.cisvan.api.domain.principal;

import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

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
}
