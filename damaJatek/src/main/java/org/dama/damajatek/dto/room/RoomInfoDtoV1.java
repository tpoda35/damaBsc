package org.dama.damajatek.dto.room;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.dama.damajatek.dto.appUser.AppUserGameDto;
import org.dama.damajatek.dto.room.chat.ChatMessageResponseDto;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RoomInfoDtoV1 {

    private Long id;
    private String name;

    private List<ChatMessageResponseDto> messages = new ArrayList<>();
    private Boolean isHost;
    private AppUserGameDto host;
    private AppUserGameDto opponent;

}
