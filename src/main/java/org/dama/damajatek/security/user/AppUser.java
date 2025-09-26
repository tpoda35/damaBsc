package org.dama.damajatek.security.user;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import lombok.*;
import org.dama.damajatek.model.Game;
import org.dama.damajatek.security.token.Token;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * User class for the whole authentication system.
 *
 * <p>It has the {@link UserDetails} implemented, which is required for the JWT system.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(
        name = "app-user",
        indexes = {
                @Index(name = "idx_app_user_email", columnList = "email")
        }
)
public class AppUser implements UserDetails {

    @Id
    @GeneratedValue
    private Long id;

    @Column(nullable = false)
    private String displayName;

    @Column(unique = true, nullable = false)
    @Email(message = "Invalid email format.")
    private String email;

    private String password;

    @Enumerated(EnumType.STRING)
    private Status status;

    @Enumerated(EnumType.STRING)
    private Role role;

    @OneToMany(mappedBy = "appUser")
    @JsonIgnore
    @ToString.Exclude
    private List<Token> tokens;

    // Games where this user played as Red
    @OneToMany(mappedBy = "redPlayer")
    private List<Game> gamesAsRed = new ArrayList<>();

    // Games where this user played as Black
    @OneToMany(mappedBy = "blackPlayer")
    private List<Game> gamesAsBlack = new ArrayList<>();

    @CreationTimestamp
    @Column(updatable = false)
    private ZonedDateTime createdAt;

    @UpdateTimestamp
    private ZonedDateTime updatedAt;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return role.getAuthorities();
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
