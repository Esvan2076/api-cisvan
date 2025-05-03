package com.cisvan.api.domain.userfollow.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FollowStatsDTO {
    
    private Long followingCount;
    private Long followersCount;
}