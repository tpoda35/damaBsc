package org.dama.damajatek.dto.game.websocket.disconnect;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.dama.damajatek.dto.game.websocket.IGameEvent;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DisconnectDto {
    private IGameEvent gameEvent;
    private Long gameId;
}
