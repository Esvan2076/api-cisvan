package com.cisvan.api.domain.name.repos;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.cisvan.api.domain.name.Name;

@Repository
public interface NameRepository extends JpaRepository<Name, String>, NameCustomRepository {

    List<Name> findByPrimaryNameContainingIgnoreCase(String primaryName);

    // Buscar múltiples NameBasics por una lista de nconst
    List<Name> findByNconstIn(List<String> nconsts);

    List<Name> findTop5ByPrimaryNameIgnoreCaseContainingOrderByPrimaryNameAsc(String namePart);

    @Query("SELECT n.primaryName FROM Name n WHERE n.nconst = :nconst")
    Optional<String> findPrimaryNameByNconst(@Param("nconst") String nconst);

    @Query("SELECT n.primaryProfession FROM Name n WHERE n.nconst = :nconst")
    Optional<List<String>> findPrimaryProfessionByNconst(@Param("nconst") String nconst);

    // New method to retrieve multiple primary names by their identifiers (nconst)
    @Query("SELECT n.nconst, n.primaryName FROM Name n WHERE n.nconst IN :nconsts")
    List<Object[]> findPrimaryNamesByNconstIn(@Param("nconsts") List<String> nconsts);

    @Query(value = "SELECT * FROM name_basics n WHERE n.nconst IN :nconsts " +
                   "AND :profession = ANY(n.primary_profession)", nativeQuery = true)
    List<Name> findByNconstInAndProfession(@Param("nconsts") List<String> nconsts, @Param("profession") String profession);
}
