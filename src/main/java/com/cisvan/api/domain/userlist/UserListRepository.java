package com.cisvan.api.domain.userlist;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserListRepository extends JpaRepository<UserList, Long> {

    List<UserList> findByUserId(Long userId);

    boolean existsByUserIdAndTitleId(Long userId, String titleId);

    void deleteByUserIdAndTitleId(Long userId, String titleId);

    Optional<UserList> findByUserIdAndTitleId(Long userId, String titleId);

    @Query("SELECT u.titleId FROM UserList u WHERE u.userId = :userId")
    List<String> findTitleIdsByUserId(@Param("userId") Long userId);
}