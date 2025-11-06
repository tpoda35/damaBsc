package org.dama.damajatek.repository;

import org.dama.damajatek.entity.Game;
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

}
