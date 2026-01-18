package org.dama.damajatek.repository;

import org.dama.damajatek.entity.room.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface IChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    List<ChatMessage> findByRoomIdOrderBySentAtAsc(Long roomId);
}
