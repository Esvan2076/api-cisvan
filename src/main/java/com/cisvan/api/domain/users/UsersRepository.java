package com.cisvan.api.domain.users;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface UsersRepository extends JpaRepository<Users, Long> {

    Optional<Users> findByUsername(String username);

    Optional<Users> findByEmail(String email);

    Optional<Users> findByUsernameOrEmail(String username, String email);

    boolean existsByEmail(String email);

    @Query("SELECT u.id FROM Users u WHERE u.username = :username")
    Long findIdByUsername(@Param("username") String username);

    // ✅ Verificación por correo usando email + código
    Optional<Users> findByEmailAndEmailVerificationCode(String email, String code);

    // ✅ Recuperación de contraseña usando email + código
    Optional<Users> findByEmailAndPasswordResetCode(String email, String code);

    Page<Users> findByUsernameContainingIgnoreCase(String username, Pageable pageable);

    List<Users> findAllByBannedTrue();
}
