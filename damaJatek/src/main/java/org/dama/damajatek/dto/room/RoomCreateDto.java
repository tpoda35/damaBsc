package org.dama.damajatek.dto.room;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RoomCreateDto {

    @NotBlank(message = "Room name cannot be blank.")
    @Length(max = 15, message = "Name max length is 15 character.")
    private String name;

    @Length(max = 30, message = "Description max length is 30 character.")
    private String description;

    private Boolean locked;
    private String password;

}
