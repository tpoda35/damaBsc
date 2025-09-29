package org.dama.damajatek.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.dama.damajatek.security.user.AppUser;

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

    private boolean locked = false; // only require password if this is set

    private String password; // hash

    @ManyToOne
    @JoinColumn(name = "host_id", nullable = false)
    private AppUser host;

    @ManyToOne
    @JoinColumn(name = "opponent_id")
    private AppUser opponent;

    private boolean started = false;

    @OneToOne(mappedBy = "room", cascade = CascadeType.ALL)
    private Game game;

}
