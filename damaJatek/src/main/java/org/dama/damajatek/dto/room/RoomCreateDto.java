package org.dama.damajatek.dto.room;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.dama.damajatek.annotation.room.ValidRoomPassword;

@ValidRoomPassword // if the room is locked, then password is required
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RoomCreateDto {

    @NotBlank(message = "Room name cannot be blank.")
    private String name;

    private boolean locked;
    private String password;

}
