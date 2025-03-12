package com.cisvan.api.component.episode;

import java.util.List;
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
}
