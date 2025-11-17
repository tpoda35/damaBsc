package org.dama.damajatek.dto.appUser;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AppUserProfileDto {

    private Long id;
    private String displayName;
    private String email;

    private Integer hostedRooms;
    private Integer joinedRooms;
    private Integer wins;
    private Integer loses;

    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
