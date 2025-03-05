package com.cisvan.api.component.principals;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.data.repository.query.Param;

@Repository
public interface TitlePrincipalsRepository extends JpaRepository<TitlePrincipals, TitlePrincipalsId> {
    @Query("SELECT ta FROM TitlePrincipals ta WHERE ta.id.tconst = :tconst")
    List<TitlePrincipals> findByTitleId(@Param("tconst") String tconst);
}
