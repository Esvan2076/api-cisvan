package com.cisvan.api.domain.title.repos;

import com.cisvan.api.domain.title.dtos.TitleKnownForDTO;
import com.cisvan.api.domain.title.dtos.searchDTO.TitleAdvancedSearchDTO;
import com.cisvan.api.domain.titlerating.TitleRating;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Repository
public class TitleCustomRepositoryImpl implements TitleCustomRepository {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    @Transactional(readOnly = true)
    public Page<TitleKnownForDTO> advancedSearch(TitleAdvancedSearchDTO criteria, Pageable pageable) {
        int offset = (int) pageable.getOffset();
        int pageSize = pageable.getPageSize();

        if (criteria.getCategory() != null) {
            String orderBy;
            switch (criteria.getCategory()) {
                case 1 -> orderBy = "tr.average_rating DESC, tr.num_votes DESC"; // Más valorados
                case 2 -> orderBy = "tr.num_votes DESC, tr.average_rating DESC"; // Más populares
                case 3 -> orderBy = "tb.start_year DESC"; // Recién añadidos
                default -> orderBy = "tb.primary_title"; // Fallback
            }

            // Filtrar sólo títulos que no sean episodios y que estén en ratings
            String sql = String.format("""
                        SELECT tb.tconst, tb.title_type, tb.primary_title, tb.start_year, tb.poster_url,
                               tr.average_rating, tr.num_votes
                        FROM title_basics tb
                        JOIN title_ratings tr ON tb.tconst = tr.tconst
                        WHERE tb.title_type != 'tvEpisode'
                        ORDER BY %s
                        OFFSET %d ROWS FETCH FIRST %d ROWS ONLY
                    """, orderBy, offset, pageSize);

            List<Object[]> resultList = getQueryResultList(entityManager.createNativeQuery(sql));
            List<TitleKnownForDTO> titles = mapToTitleKnownForDTO(resultList);

            // Total count of qualified titles
            String countSql = """
                        SELECT COUNT(*)
                        FROM title_basics tb
                        JOIN title_ratings tr ON tb.tconst = tr.tconst
                        WHERE tb.title_type != 'tvEpisode'
                    """;
            Long total = ((Number) entityManager.createNativeQuery(countSql).getSingleResult()).longValue();

            return new PageImpl<>(titles, pageable, total);
        }

        // Convertir géneros a formato de array literal de PostgreSQL
        String genresArrayLiteral = "{" + String.join(",", normalizeGenres(criteria.getGenres())) + "}";

        // Manejar el caso cuando los tipos son nulos o vacíos
        String typesArrayLiteral = "";
        if (criteria.getTypes() != null && !criteria.getTypes().isEmpty()) {
            typesArrayLiteral = criteria.getTypes().stream()
                    .map(type -> "'" + type + "'")
                    .collect(Collectors.joining(", "));
        }

        // Construcción dinámica de la cláusula WHERE
        StringBuilder whereClause = new StringBuilder();
        if (!typesArrayLiteral.isEmpty()) {
            whereClause.append("WHERE tb.title_type IN (").append(typesArrayLiteral).append(") ");
        }
        if (!genresArrayLiteral.isEmpty()) {
            if (whereClause.length() > 0) {
                whereClause.append("AND ");
            } else {
                whereClause.append("WHERE ");
            }
            whereClause.append("tb.genres @> CAST('").append(genresArrayLiteral).append("' AS VARCHAR[]) ");
        }

        // Construir la consulta nativa con los parámetros adecuados
        String sql = String.format(
                """
                            SELECT tb.tconst, tb.title_type, tb.primary_title, tb.start_year, tb.poster_url, tr.average_rating, tr.num_votes
                            FROM title_basics tb
                            LEFT JOIN title_ratings tr ON tb.tconst = tr.tconst
                            %s
                            OFFSET %d ROWS FETCH FIRST %d ROWS ONLY
                        """,
                whereClause.toString(), offset, pageSize);

        // Crear el Query nativo
        Query query = entityManager.createNativeQuery(sql);

        // Obtener los resultados con conversión segura
        List<Object[]> resultList = getQueryResultList(query);
        List<TitleKnownForDTO> titles = mapToTitleKnownForDTO(resultList);

        // Construir la consulta para el conteo total
        String countSql = String.format("""
                    SELECT COUNT(*)
                    FROM title_basics tb
                    %s
                """, whereClause.toString());

        Long total = ((Number) entityManager.createNativeQuery(countSql).getSingleResult()).longValue();

        return new PageImpl<>(titles, pageable, total);
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

    private List<String> normalizeGenres(List<String> rawGenres) {
        if (rawGenres == null)
            return List.of();
        return rawGenres.stream()
                .map(g -> g.substring(0, 1).toUpperCase() + g.substring(1).toLowerCase())
                .toList();
    }
}
