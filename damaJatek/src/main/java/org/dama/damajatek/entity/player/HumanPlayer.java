package org.dama.damajatek.entity.player;

import jakarta.persistence.*;
import lombok.*;
import org.dama.damajatek.authentication.user.AppUser;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@DiscriminatorValue("HUMAN")
public class HumanPlayer extends Player {

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id")
    private AppUser user;

    @Override
    public String getDisplayName() {
        return user != null ? user.getDisplayName() : "Unknown Player";
    }
}