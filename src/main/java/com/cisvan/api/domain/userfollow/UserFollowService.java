package com.cisvan.api.domain.userfollow;

import java.util.List;

import org.springframework.stereotype.Service;

import com.cisvan.api.domain.userfollow.dtos.FollowStatsDTO;
import com.cisvan.api.domain.users.dto.response.UserSummaryPrestigeDTO;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserFollowService {

    private final UserFollowRepository userFollowRepository;
    
    public FollowStatsDTO getFollowStats(Long userId) {
        return userFollowRepository.getFollowStats(userId);
    }

    public List<UserSummaryPrestigeDTO> getFollowersOfUser(Long userId) {
        List<Object[]> rows = userFollowRepository.findFollowersWithPrestige(userId);

        return rows.stream().map(row -> UserSummaryPrestigeDTO.builder()
                .id(((Number) row[0]).longValue())
                .username((String) row[1])
                .profileImageUrl((String) row[2])
                .currentRank(row[3] != null ? ((Number) row[3]).shortValue() : 0)
                .trendDirection(row[4] != null ? row[4].toString() : null)
                .build()
        ).toList();
    }

    public List<UserSummaryPrestigeDTO> getFollowingOfUser(Long userId) {
        List<Object[]> rows = userFollowRepository.findFollowingWithPrestige(userId);
    
        return rows.stream().map(row -> UserSummaryPrestigeDTO.builder()
                .id(((Number) row[0]).longValue())
                .username((String) row[1])
                .profileImageUrl((String) row[2])
                .currentRank(row[3] != null ? ((Number) row[3]).shortValue() : 0)
                .trendDirection(row[4] != null ? row[4].toString() : null)
                .build()
        ).toList();
    }    
}
