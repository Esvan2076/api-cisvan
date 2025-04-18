package com.cisvan.api.domain.users;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface UsersRepository extends JpaRepository<Users, Long> {

    Optional<Users> findByUsername(String username);

    @Query("SELECT u.id FROM Users u WHERE u.username = :username")
    Long findIdByUsername(@Param("username") String username);
}
