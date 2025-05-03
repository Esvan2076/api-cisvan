package com.cisvan.api.domain.users.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserSummaryPrestigeDTO {

    private Long id;
    private String username;
    private String profileImageUrl;
    private short currentRank;
    private String trendDirection;
}