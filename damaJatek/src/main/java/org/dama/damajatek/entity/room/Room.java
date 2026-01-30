package org.dama.damajatek.entity.room;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.dama.damajatek.entity.Game;
import org.dama.damajatek.enums.room.ReadyStatus;
import org.dama.damajatek.authentication.user.AppUser;

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

    @NotBlank(message = "Room name cannot be blank.")
    @Column(nullable = false)
    private String name;

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

    private Boolean started;

    @OneToOne(mappedBy = "room")
    private Game game;

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
        started = false;
    }
}
