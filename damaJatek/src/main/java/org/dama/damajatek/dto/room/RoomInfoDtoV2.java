package org.dama.damajatek.dto.room;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RoomInfoDtoV2 {

    private Long id;
    private String name;
    private String description;
    private int playerCount;
    private boolean locked;

}