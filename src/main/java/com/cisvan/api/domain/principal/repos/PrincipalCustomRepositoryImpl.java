package com.cisvan.api.domain.principal.repos;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.cisvan.api.domain.title.dtos.TitleKnownForDTO;
import com.cisvan.api.domain.titlerating.TitleRating;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;

@Repository
public class PrincipalCustomRepositoryImpl implements PrincipalCustomRepository {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    @Transactional(readOnly = true)
    public Page<TitleKnownForDTO> findWorksByPerson(String nconst, Pageable pageable) {
        int offset = (int) pageable.getOffset();
        int pageSize = pageable.getPageSize();

        // Consulta SQL para obtener las obras en las que participó la persona, excluyendo duplicados y tvEpisode
        String sql = """
            SELECT tb.tconst, tb.title_type, tb.primary_title, tb.start_year, tb.poster_url, tr.average_rating, tr.num_votes
            FROM title_principals tp
            JOIN title_basics tb ON tp.tconst = tb.tconst
            LEFT JOIN title_ratings tr ON tb.tconst = tr.tconst
            WHERE tp.nconst = :nconst
              AND tb.title_type != 'tvEpisode'
            GROUP BY tb.tconst, tb.title_type, tb.primary_title, tb.start_year, tb.poster_url, tr.average_rating, tr.num_votes
            OFFSET :offset ROWS FETCH FIRST :pageSize ROWS ONLY
        """;

        Query query = entityManager.createNativeQuery(sql)
                .setParameter("nconst", nconst)
                .setParameter("offset", offset)
                .setParameter("pageSize", pageSize);

        // Obtener los resultados
        List<Object[]> resultList = getQueryResultList(query);
        List<TitleKnownForDTO> works = mapToTitleKnownForDTO(resultList);

        // Consulta para contar el total de resultados, excluyendo duplicados y tvEpisode
        String countSql = """
            SELECT COUNT(DISTINCT tb.tconst)
            FROM title_principals tp
            JOIN title_basics tb ON tp.tconst = tb.tconst
            WHERE tp.nconst = :nconst
              AND tb.title_type != 'tvEpisode'
        """;
        Long total = ((Number) entityManager.createNativeQuery(countSql)
                .setParameter("nconst", nconst)
                .getSingleResult()).longValue();

        return new PageImpl<>(works, pageable, total);
    }

    @SuppressWarnings("unchecked")
    private List<Object[]> getQueryResultList(Query query) {
        return (List<Object[]>) query.getResultList();
    }

    private List<TitleKnownForDTO> mapToTitleKnownForDTO(List<Object[]> resultList) {
        return resultList.stream().map(result -> {
            TitleKnownForDTO dto = new TitleKnownForDTO();
            dto.setTconst((String) result[0]);
            dto.setTitleType((String) result[1]);
            dto.setPrimaryTitle((String) result[2]);

            // Conversión a Short para los años
            if (result[3] != null) {
                dto.setStartYear(((Number) result[3]).shortValue());
            }

            dto.setPosterUrl((String) result[4]);

            // Manejar el rating si está presente
            if (result[5] != null) {
                TitleRating rating = new TitleRating();
                rating.setAverageRating((BigDecimal) result[5]);
                rating.setNumVotes((Integer) result[6]);
                dto.setTitleRatings(rating);
            }

            return dto;
        }).toList();
    }
}
