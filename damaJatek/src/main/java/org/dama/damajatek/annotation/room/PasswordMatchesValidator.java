package org.dama.damajatek.annotation.room;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.dama.damajatek.authentication.auth.RegisterRequest;

public class PasswordMatchesValidator implements ConstraintValidator<PasswordMatches, RegisterRequest> {

    @Override
    public boolean isValid(RegisterRequest request, ConstraintValidatorContext context) {
        if (request.getPassword() == null || request.getConfirmPassword() == null) {
            return true; // other @NotBlank annotations handle null/blank
        }
        return request.getPassword().equals(request.getConfirmPassword());
    }
}
