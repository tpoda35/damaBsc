package org.dama.damajatek.repository;

import org.dama.damajatek.entity.room.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface IChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    @Query("SELECT m FROM ChatMessage m " +
            "LEFT JOIN FETCH m.sender " +
            "WHERE m.room.id = :roomId " +
            "ORDER BY m.sentAt")
    List<ChatMessage> findByRoomIdWithSender(@Param("roomId") Long roomId);

}
