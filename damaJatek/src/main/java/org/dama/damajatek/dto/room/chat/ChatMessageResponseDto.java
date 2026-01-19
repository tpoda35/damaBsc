package org.dama.damajatek.dto.room.chat;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ChatMessageResponseDto {

    private Long id;
    private Long senderId;
    private String senderName;
    private String content;

}
