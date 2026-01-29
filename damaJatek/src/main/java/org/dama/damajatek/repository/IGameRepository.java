package org.dama.damajatek.repository;

import org.dama.damajatek.entity.Game;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface IGameRepository extends JpaRepository<Game, Long> {

    @Query("""
        SELECT g FROM Game g
        LEFT JOIN FETCH g.redPlayer
        LEFT JOIN FETCH g.whitePlayer
        WHERE g.id = :id
    """)
    Optional<Game> findByIdWithPlayers(@Param("id") Long id);

    @Query("""
        SELECT g FROM Game g
        LEFT JOIN TREAT(g.redPlayer AS HumanPlayer) redPlayer
        LEFT JOIN TREAT(g.whitePlayer AS HumanPlayer) whitePlayer
        WHERE (redPlayer.user.id = :userId OR whitePlayer.user.id = :userId)
        ORDER BY g.startTime DESC
    """)
    Page<Game> findByPlayerId(@Param("userId") Long userId, Pageable pageable);

    @Query("""
        SELECT COUNT(g)
        FROM Game g
        LEFT JOIN TREAT(g.redPlayer AS BotPlayer) redBot
        LEFT JOIN TREAT(g.whitePlayer AS BotPlayer) whiteBot
        LEFT JOIN TREAT(g.redPlayer AS HumanPlayer) redHuman
        LEFT JOIN TREAT(g.whitePlayer AS HumanPlayer) whiteHuman
        WHERE
          (redHuman.user.id = :playerId AND whiteBot.id IS NOT NULL AND g.result IN ('RED_WIN','WHITE_FORFEIT'))
          OR
          (whiteHuman.user.id = :playerId AND redBot.id IS NOT NULL AND g.result IN ('WHITE_WIN','RED_FORFEIT'))
    """)
    Integer countWinsVsAI(@Param("playerId") Long playerId);


    @Query("""
        SELECT COUNT(g)
        FROM Game g
        LEFT JOIN TREAT(g.redPlayer AS BotPlayer) redBot
        LEFT JOIN TREAT(g.whitePlayer AS BotPlayer) whiteBot
        LEFT JOIN TREAT(g.redPlayer AS HumanPlayer) redHuman
        LEFT JOIN TREAT(g.whitePlayer AS HumanPlayer) whiteHuman
        WHERE
          (redHuman.user.id = :playerId AND whiteBot.id IS NOT NULL AND g.result IN ('WHITE_WIN','RED_FORFEIT'))
          OR
          (whiteHuman.user.id = :playerId AND redBot.id IS NOT NULL AND g.result IN ('RED_WIN','WHITE_FORFEIT'))
    """)
    Integer countLossesVsAI(@Param("playerId") Long playerId);



    @Query("""
        SELECT COUNT(g)
        FROM Game g
        LEFT JOIN TREAT(g.redPlayer AS HumanPlayer) redHuman
        LEFT JOIN TREAT(g.whitePlayer AS HumanPlayer) whiteHuman
        WHERE
          (redHuman.user.id = :playerId AND whiteHuman.user.id IS NOT NULL AND g.result IN ('RED_WIN', 'WHITE_FORFEIT'))
          OR
          (whiteHuman.user.id = :playerId AND redHuman.user.id IS NOT NULL AND g.result IN ('WHITE_WIN', 'RED_FORFEIT'))
    """)
    Integer countWinsVsPlayer(@Param("playerId") Long playerId);


    @Query("""
        SELECT COUNT(g)
        FROM Game g
        LEFT JOIN TREAT(g.redPlayer AS HumanPlayer) redHuman
        LEFT JOIN TREAT(g.whitePlayer AS HumanPlayer) whiteHuman
        WHERE
            (redHuman.user.id = :playerId AND whiteHuman.user.id IS NOT NULL AND g.result IN ('white_WIN', 'RED_FORFEIT'))
            OR
            (whiteHuman.user.id = :playerId AND redHuman.user.id IS NOT NULL AND g.result IN ('RED_WIN', 'white_FORFEIT'))
    """)
    Integer countLossesVsPlayer(@Param("playerId") Long playerId);

    @Query("""
        SELECT COALESCE(COUNT(g), 0)
        FROM Game g
        LEFT JOIN TREAT(g.redPlayer AS HumanPlayer) redHuman
        LEFT JOIN TREAT(g.whitePlayer AS BotPlayer) whiteBot
        WHERE (redHuman.user.id = :playerId AND whiteBot IS NOT NULL AND g.result = 'DRAW')
           OR (whiteBot IS NOT NULL AND redHuman.user.id = :playerId AND g.result = 'DRAW')
    """)
    Integer countDrawsVsAI(@Param("playerId") Long playerId);


    @Query("""
        SELECT COALESCE(COUNT(g), 0)
        FROM Game g
        LEFT JOIN TREAT(g.redPlayer AS HumanPlayer) redHuman
        LEFT JOIN TREAT(g.whitePlayer AS HumanPlayer) whiteHuman
        WHERE (redHuman.user.id = :playerId AND whiteHuman.id IS NOT NULL AND g.result = 'DRAW')
           OR (whiteHuman.user.id = :playerId AND redHuman.id IS NOT NULL AND g.result = 'DRAW')
    """)
    Integer countDrawsVsPlayer(@Param("playerId") Long playerId);

}
