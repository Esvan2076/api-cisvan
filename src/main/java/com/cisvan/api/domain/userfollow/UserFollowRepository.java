package com.cisvan.api.domain.userfollow;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.cisvan.api.domain.userfollow.dtos.FollowStatsDTO;

@Repository
public interface UserFollowRepository extends JpaRepository<UserFollow, Long> {

    @Query("""
        SELECT new com.cisvan.api.domain.userfollow.dtos.FollowStatsDTO(
            COUNT(CASE WHEN f.followerId = :userId THEN 1 END),
            COUNT(CASE WHEN f.followedId = :userId THEN 1 END)
        )
        FROM UserFollow f
        WHERE f.followerId = :userId OR f.followedId = :userId
    """)
    FollowStatsDTO getFollowStats(@Param("userId") Long userId);

    @Query(value = """
        SELECT 
            u.id AS id,
            u.username,
            u.profile_image_url,
            p.current_rank,
            p.trend_direction
        FROM user_follow f
        JOIN users u ON u.id = f.follower_id
        LEFT JOIN user_prestige p ON u.id = p.user_id
        WHERE f.followed_id = :userId
    """, nativeQuery = true)
    List<Object[]> findFollowersWithPrestige(@Param("userId") Long userId);

    @Query(value = """
        SELECT 
            u.id AS id,
            u.username,
            u.profile_image_url,
            p.current_rank,
            p.trend_direction
        FROM user_follow f
        JOIN users u ON u.id = f.followed_id
        LEFT JOIN user_prestige p ON u.id = p.user_id
        WHERE f.follower_id = :userId
    """, nativeQuery = true)
    List<Object[]> findFollowingWithPrestige(@Param("userId") Long userId);

    // Verifica si ya existe la relación
    boolean existsByFollowerIdAndFollowedId(Long followerId, Long followedId);

    // Elimina una relación de seguimiento
    int deleteByFollowerIdAndFollowedId(Long followerId, Long followedId);

}
