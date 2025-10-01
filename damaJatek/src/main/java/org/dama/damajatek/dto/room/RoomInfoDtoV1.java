package org.dama.damajatek.dto.room;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RoomInfoDtoV1 {

    private Long id;
    private String name;
    private String host;
    private boolean started;
    private boolean locked;

}
