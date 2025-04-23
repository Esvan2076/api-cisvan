package com.cisvan.api.domain.name.repos;

import com.cisvan.api.domain.name.Name;
import com.cisvan.api.domain.name.dto.NameAdvancedSearchDTO;

import jakarta.persistence.*;
import jakarta.persistence.criteria.*;

import org.springframework.data.domain.*;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Repository
public class NameCustomRepositoryImpl implements NameCustomRepository {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    @Transactional(readOnly = true)
    public Page<Name> advancedSearch(NameAdvancedSearchDTO criteria, Pageable pageable) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();

        // Main query
        CriteriaQuery<Name> query = cb.createQuery(Name.class);
        Root<Name> name = query.from(Name.class);
        List<Predicate> predicates = buildPredicates(criteria, cb, query, name);

        query.where(cb.and(predicates.toArray(new Predicate[0])));
        TypedQuery<Name> typedQuery = entityManager.createQuery(query);
        typedQuery.setFirstResult((int) pageable.getOffset());
        typedQuery.setMaxResults(pageable.getPageSize());
        List<Name> results = typedQuery.getResultList();

        // Count query
        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        Root<Name> countRoot = countQuery.from(Name.class);
        countQuery.select(cb.count(countRoot));
        countQuery.where(cb.and(buildPredicates(criteria, cb, countQuery, countRoot).toArray(new Predicate[0])));
        Long total = entityManager.createQuery(countQuery).getSingleResult();

        return new PageImpl<>(results, pageable, total);
    }

    private List<Predicate> buildPredicates(NameAdvancedSearchDTO criteria, CriteriaBuilder cb,
                                            CriteriaQuery<?> query, Root<Name> name) {
        List<Predicate> predicates = new ArrayList<>();

        if (criteria.getName() != null && !criteria.getName().isBlank()) {
            String likePattern = "%" + criteria.getName().toLowerCase() + "%";
            predicates.add(cb.like(cb.lower(name.get("primaryName")), likePattern));
        }

        if (criteria.getProfessions() != null && !criteria.getProfessions().isEmpty()) {
            String[] professionsArray = criteria.getProfessions().toArray(new String[0]);

            Predicate professionOverlap = cb.isTrue(
                cb.function("array_overlap", Boolean.class,
                    name.get("primaryProfession"),
                    cb.literal(professionsArray)
                )
            );
            predicates.add(professionOverlap);
        }

        return predicates;
    }
}
