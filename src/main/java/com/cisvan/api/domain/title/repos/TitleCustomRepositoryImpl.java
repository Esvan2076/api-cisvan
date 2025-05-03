package com.cisvan.api.domain.title.repos;

import com.cisvan.api.domain.streaming.Streaming;
import com.cisvan.api.domain.title.Title;
import com.cisvan.api.domain.title.dtos.searchDTO.TitleAdvancedSearchDTO;
import com.cisvan.api.domain.titlestream.TitleStream;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.*;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Repository
public class TitleCustomRepositoryImpl implements TitleCustomRepository {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    @Transactional(readOnly = true)
    public Page<Title> advancedSearch(TitleAdvancedSearchDTO criteria, Pageable pageable) {

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();

        // ----------- MAIN QUERY -----------
        CriteriaQuery<Title> query = cb.createQuery(Title.class);
        Root<Title> title = query.from(Title.class);
        List<Predicate> predicates = buildPredicates(criteria, cb, query, title);

        query.where(cb.and(predicates.toArray(new Predicate[0])));
        TypedQuery<Title> typedQuery = entityManager.createQuery(query);
        typedQuery.setFirstResult((int) pageable.getOffset());
        typedQuery.setMaxResults(pageable.getPageSize());
        List<Title> results = typedQuery.getResultList();

        // ----------- COUNT QUERY -----------
        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        Root<Title> countRoot = countQuery.from(Title.class);
        countQuery.select(cb.count(countRoot));
        List<Predicate> countPredicates = buildPredicates(criteria, cb, countQuery, countRoot);
        countQuery.where(cb.and(countPredicates.toArray(new Predicate[0])));
        Long total = entityManager.createQuery(countQuery).getSingleResult();

        results.forEach(t -> System.out.println("Título: " + t.getPrimaryTitle() + " | TCONST: " + t.getTconst()));

        return new PageImpl<>(results, pageable, total);
    }

    private List<Predicate> buildPredicates(
            TitleAdvancedSearchDTO criteria,
            CriteriaBuilder cb,
            CriteriaQuery<?> query,
            Root<Title> title
    ) {
        List<Predicate> predicates = new ArrayList<>();

        if (criteria.getName() != null && !criteria.getName().isBlank()) {
            String pattern = "%" + criteria.getName().toLowerCase() + "%";
            predicates.add(cb.like(cb.lower(title.get("primaryTitle")), pattern));
        }

        if (criteria.getTypes() != null && !criteria.getTypes().isEmpty()) {
            predicates.add(title.get("titleType").in(criteria.getTypes()));
        }

        if (criteria.getGenres() != null && !criteria.getGenres().isEmpty()) {
            List<String> normalizedGenres = normalizeGenres(criteria.getGenres());
            System.out.println("Géneros normalizados: " + normalizedGenres);
        
            Predicate genreOverlap = cb.isTrue(cb.function(
                "array_overlap",
                Boolean.class,
                title.get("genres"),
                cb.literal(normalizedGenres.toArray(new String[0]))
            ));
            predicates.add(genreOverlap);
        }        

        if (criteria.getStreamingServices() != null && !criteria.getStreamingServices().isBlank()) {
            Subquery<String> subquery = query.subquery(String.class);
            Root<TitleStream> ts = subquery.from(TitleStream.class);

            Subquery<Integer> inner = query.subquery(Integer.class);
            Root<Streaming> s = inner.from(Streaming.class);
            inner.select(s.get("id"))
                .where(cb.equal(cb.lower(s.get("name")), criteria.getStreamingServices().toLowerCase()));

            subquery.select(ts.get("titleStreamId").get("tconst"))
                    .where(ts.get("titleStreamId").get("streamingId").in(inner));

            predicates.add(title.get("tconst").in(subquery));
        }

        return predicates;
    }

    // <-- Aquí va el nuevo método
    private List<String> normalizeGenres(List<String> rawGenres) {
        return rawGenres.stream()
            .map(g -> g.substring(0, 1).toUpperCase() + g.substring(1).toLowerCase())
            .toList();
    }
}
