package org.dama.damajatek.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.dama.damajatek.enums.room.ReadyStatus;
import org.dama.damajatek.authentication.user.AppUser;

import static org.dama.damajatek.enums.room.ReadyStatus.NOT_READY;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "rooms")
public class Room {

    @Id
    @GeneratedValue
    private Long id;

    @NotBlank(message = "Room name cannot be blank.")
    @Column(nullable = false)
    private String name;

    // If this is true, then the room requires password
    private boolean locked = false;

    // This is hashed with bcrypt
    private String password;

    @ManyToOne
    @JoinColumn(name = "host_id", nullable = false)
    private AppUser host;

    private ReadyStatus hostReadyStatus = NOT_READY;

    @ManyToOne
    @JoinColumn(name = "opponent_id")
    private AppUser opponent = null;

    private ReadyStatus opponentReadyStatus = NOT_READY;

    private boolean started = false;

    @OneToOne(mappedBy = "room")
    private Game game;

    @Version
    private Long version;

    @Transient
    public boolean isFullyReady() {
        return hostReadyStatus == ReadyStatus.READY && opponentReadyStatus == ReadyStatus.READY;
    }
}
