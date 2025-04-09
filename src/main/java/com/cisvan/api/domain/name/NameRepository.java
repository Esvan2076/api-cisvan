package com.cisvan.api.domain.name;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NameRepository extends JpaRepository<Name, String> {

    List<Name> findByPrimaryNameContainingIgnoreCase(String primaryName);

    // Buscar múltiples NameBasics por una lista de nconst
    List<Name> findByNconstIn(List<String> nconsts);

    List<Name> findTop5ByPrimaryNameIgnoreCaseContainingOrderByPrimaryNameAsc(String namePart);
}
