package org.dama.damajatek.entity.room;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.dama.damajatek.authentication.user.AppUser;
import org.dama.damajatek.enums.room.ReadyStatus;

import java.util.ArrayList;
import java.util.List;

import static org.dama.damajatek.enums.room.ReadyStatus.NOT_READY;
import static org.dama.damajatek.enums.room.ReadyStatus.READY;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(
        name = "rooms",
        indexes = {
                @Index(name = "idx_room_host", columnList = "host_id"),
                @Index(name = "idx_room_opponent", columnList = "opponent_id")
        }
)
public class Room {

    @Id
    @GeneratedValue
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(length = 30)
    private String description;

    // If this is true, then the room requires password
    private Boolean locked;

    // This is hashed with bcrypt
    private String password;

    @ManyToOne
    @JoinColumn(name = "host_id", nullable = false)
    private AppUser host;

    private ReadyStatus hostReadyStatus;

    @ManyToOne
    @JoinColumn(name = "opponent_id")
    private AppUser opponent;

    private ReadyStatus opponentReadyStatus;

    @OneToMany(mappedBy = "room", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ChatMessage> chatMessages = new ArrayList<>();

    @Version
    private Long version;

    @Transient
    public boolean isFullyReady() {
        return hostReadyStatus == READY && opponentReadyStatus == READY;
    }

    @PrePersist
    public void init() {
        hostReadyStatus = NOT_READY;
        opponentReadyStatus = NOT_READY;
    }
}
