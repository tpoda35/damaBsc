package org.dama.damajatek.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.dama.damajatek.entity.Game;
import org.dama.damajatek.model.Board;

@Slf4j
public class BoardSerializer {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static Board loadBoard(Game game) {
        try {
            return objectMapper.readValue(game.getBoardState(), Board.class);
        } catch (JsonProcessingException e) {
            log.error("Failed to load board for game {}: {}", game.getId(), e.getMessage());
            throw new RuntimeException("Failed to load board", e);
        }
    }

    public static void saveBoard(Game game, Board board) {
        try {
            game.setBoardState(objectMapper.writeValueAsString(board));
        } catch (JsonProcessingException e) {
            log.error("Failed to save board for game {}: {}", game.getId(), e.getMessage());
            throw new RuntimeException("Failed to save board", e);
        }
    }
}

