package org.dama.damajatek.repository;

import org.dama.damajatek.authentication.user.AppUser;
import org.dama.damajatek.entity.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoomRepository extends JpaRepository<Room, Long> {
    @Query("SELECT r FROM Room r " +
            "LEFT JOIN FETCH r.host " +
            "LEFT JOIN FETCH r.opponent " +
            "WHERE r.id = :roomId")
    Optional<Room> findByIdWithUsers(@Param("roomId") Long roomId);
    @Query("SELECT r FROM Room r " +
            "LEFT JOIN FETCH r.host " +
            "LEFT JOIN FETCH r.opponent " +
            "WHERE r.started = false AND" +
            " r.host = :user OR r.opponent = :user")
    Optional<Room> findByHostOrOpponent(@Param("user") AppUser user);
}
