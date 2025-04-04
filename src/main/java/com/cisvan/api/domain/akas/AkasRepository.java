package com.cisvan.api.domain.akas;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface AkasRepository extends JpaRepository<Akas, AkasId> {

    @Query("SELECT ta FROM Akas ta WHERE ta.id.tconst = :tconst")
    List<Akas> findByTconst(@Param("tconst") String tconst);
}