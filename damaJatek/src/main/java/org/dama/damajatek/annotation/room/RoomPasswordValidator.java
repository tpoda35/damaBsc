package org.dama.damajatek.annotation.room;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.dama.damajatek.dto.room.RoomCreateDto;

public class RoomPasswordValidator implements ConstraintValidator<ValidRoomPassword, RoomCreateDto> {

    @Override
    public boolean isValid(RoomCreateDto dto, ConstraintValidatorContext context) {
        if (dto == null) {
            return true;
        }

        if (dto.isLocked() && (dto.getPassword() == null || dto.getPassword().isBlank())) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("Password must be provided when room is locked.")
                    .addPropertyNode("password")
                    .addConstraintViolation();
            return false;
        }

        return true;
    }
}
