package com.cisvan.api.domain.users.dto.response;

import com.cisvan.api.domain.userfollow.dtos.FollowStatsDTO;
import com.cisvan.api.domain.userprestige.dtos.UserPrestigeDTO;
import com.cisvan.api.domain.users.Users;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserProfileDTO {
    
    private Long id;
    private String username;

    private FollowStatsDTO followStats;

    private UserPrestigeDTO userPrestigeDTO;

    @Builder.Default
    private String profileImageUrl = "https://cisvan.s3.us-west-1.amazonaws.com/1.jpg";

    public static UserProfileDTO fromEntity(Users user) {
        return UserProfileDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .profileImageUrl(user.getProfileImageUrl() != null 
                    ? user.getProfileImageUrl() 
                    : "https://cisvan.s3.us-west-1.amazonaws.com/1.jpg")
                .build();
    }
}
