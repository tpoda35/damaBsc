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
        LEFT JOIN FETCH g.blackPlayer
        WHERE g.id = :id
    """)
    Optional<Game> findByIdWithPlayers(@Param("id") Long id);

    @Query("""
        SELECT g FROM Game g
        LEFT JOIN TREAT(g.redPlayer AS HumanPlayer) redPlayer
        LEFT JOIN TREAT(g.blackPlayer AS HumanPlayer) blackPlayer
        WHERE (redPlayer.user.id = :userId OR blackPlayer.user.id = :userId)
        ORDER BY g.startTime DESC
    """)
    Page<Game> findByPlayerId(@Param("userId") Long userId, Pageable pageable);

//    @Query("SELECT COUNT(g) FROM Game g WHERE g.winner.user.id = :userId")
//    Integer countByWinnerUserId(@Param("userId") Long userId);
//
//    @Query("SELECT COUNT(g) FROM Game g WHERE g.status = org.dama.damajatek.enums.game.GameStatus.FINISHED AND ((g.redPlayer.user.id = :userId AND g.winner.user.id <> :userId) OR (g.blackPlayer.user.id = :userId AND g.winner.user.id <> :userId))")
//    Integer countByLoserUserId(@Param("userId") Long userId);

}
