package com.cisvan.api.domain.users.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class UserSummaryPrestigeExtendedDTO extends UserSummaryPrestigeDTO {

    private Boolean following;
    private Boolean mySelf;

    @Builder(builderMethodName = "extendedBuilder")
    public UserSummaryPrestigeExtendedDTO(
            Long id,
            String username,
            String profileImageUrl,
            short currentRank,
            String trendDirection,
            Boolean following,
            Boolean mySelf
    ) {
        super(id, username, profileImageUrl, currentRank, trendDirection);
        this.following = following;
        this.mySelf = mySelf;
    }
}
