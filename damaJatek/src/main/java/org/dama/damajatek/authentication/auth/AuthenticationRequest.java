package org.dama.damajatek.authentication.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AuthenticationRequest {

    @NotBlank(message = "Email field cannot be blank.")
    @Email(message = "Invalid email address format.")
    private String email;

    @NotBlank(message = "Password field cannot be blank.")
    private String password;
}
