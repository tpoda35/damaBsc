package org.dama.damajatek.dto.game.websocket;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.dama.damajatek.enums.game.GameWsAction;

import java.util.ArrayList;
import java.util.List;

import static org.dama.damajatek.enums.game.GameWsAction.CAPTURE_MADE;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CaptureMadeEventDto implements GameEventDto{
    @Builder.Default
    private GameWsAction action = CAPTURE_MADE;

    private int fromRow;
    private int fromCol;
    private int toRow;
    private int toCol;

    @Builder.Default
    private List<int[]> capturedPieces = new ArrayList<>();

}
