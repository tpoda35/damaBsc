package org.dama.damajatek.dto.game.websocket;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.dama.damajatek.enums.game.GameWsAction;
import org.dama.damajatek.model.Move;

import static org.dama.damajatek.enums.game.GameWsAction.CAPTURE_MADE;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CaptureMadeEventDto implements GameEventDto {

    @Builder.Default
    private GameWsAction action = CAPTURE_MADE;

    private Move move;

}
