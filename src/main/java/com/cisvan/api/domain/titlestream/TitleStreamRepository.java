package com.cisvan.api.domain.titlestream;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface TitleStreamRepository extends JpaRepository<TitleStream, TitleStreamId>{
    
    // Obtener los IDs de streaming por tconst
    @Query("SELECT ts.id.streamingId FROM TitleStream ts WHERE ts.id.tconst = :tconst")
    List<Integer> findStreamingIdsByTconst(@Param("tconst") String tconst);
}
