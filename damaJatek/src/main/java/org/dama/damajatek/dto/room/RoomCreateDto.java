package org.dama.damajatek.dto.room;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RoomCreateDto {

    @NotBlank(message = "Room name cannot be blank.")
    private String name;

    private Boolean locked;
    private String password;

}
