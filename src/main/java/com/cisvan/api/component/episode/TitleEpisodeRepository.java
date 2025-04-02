package com.cisvan.api.component.episode;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface TitleEpisodeRepository extends JpaRepository<TitleEpisode, String> {
    
    // Buscar episodios por `parent_tconst`
    List<TitleEpisode> findByParentTconst(String parentTconst);

    // Buscar episodios por `parent_tconst` y `season_number`
    @Query(value = "SELECT * FROM title_episode WHERE parent_tconst = :parentTconst AND season_number = :seasonNumber", nativeQuery = true)
    List<TitleEpisode> findByParentTconstAndSeasonNumber(@Param("parentTconst") String parentTconst, @Param("seasonNumber") Short seasonNumber);
    
    // Buscar el parent_tconst de un episodio específico
    @Query("SELECT e.parentTconst FROM TitleEpisode e WHERE e.tconst = :tconst")
    Optional<String> findParentTconstByEpisodeTconst(@Param("tconst") String tconst);

    @Query(value = "SELECT COUNT(DISTINCT season_number) FROM title_episode WHERE parent_tconst = :parentTconst", nativeQuery = true)
    Integer countDistinctSeasonsByParentTconst(@Param("parentTconst") String parentTconst);

}
