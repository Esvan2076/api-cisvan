package com.cisvan.api.component.akas;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface TitleAkasRepository extends JpaRepository<TitleAkas, TitleAkasId> {

    @Query("SELECT ta FROM TitleAkas ta WHERE ta.id.tconst = :tconst")
    List<TitleAkas> findByTconst(@Param("tconst") String tconst);
}