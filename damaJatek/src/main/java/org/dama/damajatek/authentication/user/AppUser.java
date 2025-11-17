package org.dama.damajatek.authentication.user;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import org.dama.damajatek.authentication.token.Token;
import org.dama.damajatek.entity.Room;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

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

    @Column(nullable = false)
    @NotBlank(message = "Password cannot be empty.")
    private String password;

    @Enumerated(EnumType.STRING)
    private Status status;

    @Enumerated(EnumType.STRING)
    private Role role;

    @OneToMany(mappedBy = "appUser")
    @JsonIgnore
    @ToString.Exclude
    private List<Token> tokens;

    // Room part
    @OneToMany(mappedBy = "host")
    @JsonIgnore
    @ToString.Exclude
    private List<Room> hostedRooms = new ArrayList<>();

    @OneToMany(mappedBy = "opponent")
    @JsonIgnore
    @ToString.Exclude
    private List<Room> joinedRooms = new ArrayList<>();

    @CreationTimestamp
    @Column(updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    private OffsetDateTime updatedAt;

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
