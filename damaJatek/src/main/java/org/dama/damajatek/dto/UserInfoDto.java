package org.dama.damajatek.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

/**
 * Data transfer object to transfer specific user data.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserInfoDto {
    private Long userId;
    private String displayName;
    private OffsetDateTime createdAt;
}
