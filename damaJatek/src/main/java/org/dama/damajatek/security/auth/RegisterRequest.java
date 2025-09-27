package org.dama.damajatek.security.auth;

import jakarta.persistence.PrePersist;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RegisterRequest {

    @NotBlank(message = "DisplayName field cannot be blank.")
    private String displayName;
    @NotBlank(message = "Email field cannot be blank.")
    @Email(message = "Invalid Email format.")
    private String email;
    private String password;
    private String confirmPassword;

    @PrePersist
    protected void checkPasswords(){
        if (!password.equals(confirmPassword)){
            throw new BadCredentialsException("Passwords does not match.");
        }
    }
}
