package com.cisvan.api.domain.crew;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface CrewRepository extends JpaRepository<Crew, String> {

    @Query(
        value = "SELECT c.tconst FROM title_crew c " +
                "JOIN title_basics t ON c.tconst = t.tconst " +
                "WHERE :director = ANY(c.directors) " +
                "AND t.title_type != 'tvEpisode'",
        nativeQuery = true
    )
    List<String> findTitlesByDirector(@Param("director") String director);
}
