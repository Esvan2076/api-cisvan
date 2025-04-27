package com.cisvan.api.domain.profileImage;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ProfileImageRepository extends JpaRepository<ProfileImage, Long> {

    @Query("""
        SELECT u FROM ProfileImage u
        WHERE u.userId = :userId
        ORDER BY u.createdAt DESC
    """)
    List<ProfileImage> findByUserId(@Param("userId") Long userId);
}
